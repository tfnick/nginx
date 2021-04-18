# build
docker build -t tfnick/nginx-proxy:v0.0.1 .

# check image is ok
```
docker images
```
will display info like this
```
tfnick/nginx-proxy         v0.0.1         eef377357d02     38 seconds ago      348MB
```

# deploy

## in kind k8s

```
kubectl create -f kind-nginx.yaml
```

```shell
deployment.apps/nginx created
service/nginx created
```

## in docker only
### run
docker run --name nginx --restart always -d -p 7443:7443 -p 7022:7022 tfnick/nginx-proxy:v0.0.1 
### validation
curl --key /opt/paas-deploy/cert/pem/admin-key.pem --cert /opt/paas-deploy/cert/pem/admin.pem --cacert /opt/paas-deploy/cert/pem/ca.pem https://127.0.0.1:6443/api/v1/nodes --verbose

# refer

http://blog.allen-mo.com/2018/04/16/nginx_apiserver_proxy/
https://www.cnblogs.com/ddrsql/p/13367771.html