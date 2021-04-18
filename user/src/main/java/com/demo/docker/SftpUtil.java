package com.demo.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SftpUtil implements AutoCloseable{
    private final Logger LOGGER = LoggerFactory.getLogger(SftpUtil.class);

    //创建sftp通信通道
    private ChannelSftp sftp = null;
    private Session session = null;
    //文件分割符，默认为linux系统
    private static final String SEPARATOR = "/";


    //SFTP协议
    private final String SFTP_PROTOCAL = "sftp";
    //默认登陆超时时间15s
    private final int LOGON_TIME_OUT = 15*1000;
    // //默认session中socket超时时间30分钟
    private final int SOCKET_TIME_OUT = 30*60*60*1000;

    /**
     * 根据输入参数获取SFTP链接
     * @param host 主机IP
     * @param username 主机登陆用户名
     * @param password 主机登陆密码
     * @param port 主机ssh登陆端口，如果port <= 0取默认值(22)
     * @param timeOut 登录超时，通道连接超时时间
     * @throws Exception
     */
    public boolean connect(String host, String username, String password, int port,int timeOut) throws Exception {
        boolean result = false;
        Channel channel = null;
        JSch jsch = new JSch();
        if (timeOut < 0) {
            timeOut = LOGON_TIME_OUT;
        }
        session = createSession(jsch, host, username, port);
// 设置登陆主机的密码
        session.setPassword(password);
        session.setTimeout(SOCKET_TIME_OUT);
// 设置登陆超时时间
        session.connect(timeOut);
        try {
// 创建sftp通信通道
            channel = (Channel) session.openChannel(SFTP_PROTOCAL);
            channel.connect(timeOut);
            sftp = (ChannelSftp) channel;
//设置根路径，远程以及本地，对于window系统不适用，window需要写绝对路径到盘符
            sftp.cd(SEPARATOR);
            sftp.lcd(SEPARATOR);
            result = true;
        } catch (JSchException e) {
            LOGGER.error("exception when channel create. "+e.getMessage(), e);
            throw new RuntimeException("exception when channel create. "+e.getMessage(), e);
        }
        return result;
    }

    /**
     * Private/public key authorization (加密秘钥方式登陆)
     * @param username 主机登陆用户名(user account)
     * @param host 主机IP(server host)
     * @param port 主机ssh登陆端口(ssh port), 如果port<=0, 取默认值22
     * @param privateKey 秘钥文件路径(the path of key file.)
     * @param passphrase 密钥的密码(the password of key file.)
     * @param timeOut 登录超时，通道连接超时时间
     * @throws Exception
     */
    public boolean connect(String username, String host, int port, String privateKey, String passphrase,int timeOut)
            throws Exception {
        boolean result = false;
        Channel channel = null;
        JSch jsch = new JSch();
        if (timeOut < 0) {
            timeOut = LOGON_TIME_OUT;
        }
// 设置密钥和密码 ,支持密钥的方式登陆
        if (!StringUtils.isEmpty(privateKey)) {
            if (!StringUtils.isEmpty(passphrase)) {
// 设置带口令的密钥
                jsch.addIdentity(privateKey, passphrase);
            } else {
// 设置不带口令的密钥
                jsch.addIdentity(privateKey);
            }
        }
        Session session = createSession(jsch, host, username, port);
// 设置登陆超时时间
        session.connect(timeOut);
        try {
// 创建sftp通信通道
            channel = (Channel) session.openChannel(SFTP_PROTOCAL);
            channel.connect(timeOut);
            sftp = (ChannelSftp) channel;
//设置根路径，远程以及本地，对于window系统不适用，window需要写绝对路径到盘符
            sftp.cd(SEPARATOR);
            sftp.lcd(SEPARATOR);
            result = true;
        } catch (JSchException e) {
            LOGGER.error("exception when channel create."+e.getMessage(), e);
            throw e;
        }
        return result;
    }

    /**
     * upload the file to the server<br/>
     * 将本地文件名为 localFile 的文件上传到目标服务器, 目标文件名为 destFile,<br/>
     * 采用默认的传输模式： OVERWRITE 覆盖式推送
     * @param localFile 本地文件的绝对路径
     * @param destFile 目标文件的绝对路径
     * @throws Exception
     */
    public boolean upload(String localFile, String destFile) throws Exception {
        boolean result = false;
        try {
            File file = new File(localFile);
            if (file.isDirectory()) {
                for (String fileName : file.list()) {
                    sftp.put(this.setDirectoryFile(localFile, fileName), destFile,ChannelSftp.OVERWRITE);
                }
            } else {
                sftp.put(localFile, destFile, ChannelSftp.OVERWRITE);
            }
            result = true;
        } catch (Exception e) {
            LOGGER.error(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
            throw new Exception(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
        }
        return result;
    }

    /**
     * upload the file to the server<br/>
     * 将本地文件名为 localFile 的文件上传到目标服务器, 目标路径为destPath,<br/>
     * 目标文件名称为destFileName
     * 采用默认的传输模式： OVERWRITE 覆盖式推送
     * @param localFile 本地文件的绝对路径
     * @param destPath 目标文件的绝对目录路径
     * @param destFileName 目标文件名称
     * @throws Exception
     */
    public boolean upload(String localFile, String destPath,String destFileName) throws Exception {
        boolean result = false;
        String destFile = "";
        try {
//创建远程路径
            createRemotedir(destPath);
//拼接远程路径+文件名
            destFile = this.setDirectoryFile(destPath, destFileName);
            sftp.put(localFile,destFile,ChannelSftp.OVERWRITE);
            result = true;
        } catch (Exception e) {
            LOGGER.error(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
            throw new Exception(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
        }
        return result;
    }


    /**
     * upload the file to the server and rename file<br/>
     * 将本地文件名为 localFile 的文件上传到目标服务器, 目标路径为destPath,<br/>
     * 目标文件名称为destFileName，重命名后的文件名称为newFileName
     * 采用默认的传输模式： OVERWRITE 覆盖式推送
     * @param localFile 本地文件的绝对路径
     * @param destPath 目标文件的绝对目录路径
     * @param destFileName 目标文件名称
     * @param newFileName 重命名后的名称
     * @throws Exception
     */
    public boolean upload(String localFile, String destPath,String destFileName,String newFileName) throws Exception {
        boolean result = false;
        String destFile = destPath;
        String newFile = "";
        try {
            //创建远程路径
            createRemotedir(destPath);
            //拼接远程路径+文件名
            destFile = this.setDirectoryFile(destPath, destFileName);
            sftp.put(localFile,destFile,ChannelSftp.OVERWRITE);
            //新文件名不为空，拼接重命名后的新文件,并重新命名
            if(!StringUtils.isEmpty(newFileName)){
                newFile = this.setDirectoryFile(destPath, newFileName);
                sftp.rename(destFile, newFile);
            }
            result =true;
        } catch (Exception e) {
            LOGGER.error(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
            throw new Exception(localFile+" upload to "+destFile+" exception "+e.getMessage(), e);
        }
        return result;
    }
    /**
     * 使用sftp下载文件,若本地存储路径下存在与下载重名的文件,忽略这个文件
     * @param remotePath 服务器上源文件的路径, 必须是目录
     * @param savePath 下载后文件的存储路径, 必须是目录
     * @param remoteFileName 服务器上的文件名称
     * @throws Exception
     */
    public boolean download(String remotePath, String savePath,String remoteFileName) throws Exception {
        boolean result = false;
        try {
//切换到远程对应remotePath目录下
            sftp.cd(remotePath);
//创建本地目录savePath
            createLocalDir(savePath);
            File localFile = new File(this.setDirectoryFile(savePath, remoteFileName));
// savePath路径下已有文件与下载文件重名, 忽略这个文件
            if (localFile.exists() && localFile.isFile()) {
                return true;
            }
//下载远程文件到savePath，文件名称为remoteFile
            sftp.get(remoteFileName, localFile+"");
            result = true;
        } catch (Exception e) {
            LOGGER.error(remotePath+SEPARATOR+remoteFileName+" download to "+savePath+" exception "+e.getMessage(), e);
            throw new Exception(remotePath+SEPARATOR+remoteFileName+" download to "+savePath+" exception " +e.getMessage(), e);
        }
        return result;
    }

    /**
     * sftp下载目标服务器上remotePath目录下所有指定的文件.<br/>
     * 若本地存储路径下存在与下载重名的文件,忽略这个文件.<br/>
     * @param remotePath 服务器上源文件的路径, 必须是目录
     * @param savePath 文件下载到本地存储的路径,必须是目录
     * @param fileList 指定的要下载的文件名列表
     * @throws Exception
     */
    public boolean downloadFileList(String remotePath, String savePath,List<String> fileList) throws Exception {
        boolean result = false;
        try {
            sftp.cd(remotePath);
            String localFile = "";
            for (String srcFile : fileList) {
                try {
                    localFile = this.setDirectoryFile(savePath, savePath);
                    File file = new File(localFile);
// savePath路径下已有文件与下载文件重名, 忽略这个文件
                    if (file.exists() && file.isFile()) {
                        continue;
                    }
                    sftp.get(srcFile, localFile);
                } catch (Exception e) {
                    LOGGER.error(remotePath + SEPARATOR + srcFile+ " download to " + localFile + " exception "+e.getMessage(), e);
                }
            }
            result = true;
        } catch (Exception e) {
            LOGGER.error(remotePath + " download to " + savePath + " exception "+e.getMessage(), e);
            throw new Exception(remotePath + " download to " + savePath + " exception "+e.getMessage(), e);
        }
        return result;
    }

    /**
     * 删除文件
     * @param dirPath 要删除文件所在目录
     * @param file 要删除的文件
     * @throws SftpException
     */
    public boolean delete(String dirPath, String file) throws SftpException {
        sftp.cd(SEPARATOR);
        String now = sftp.pwd();
        sftp.cd(dirPath);
        sftp.rm(file);
        sftp.cd(now);
        return true;
    }

    /**
     * 删除文件
     * @param filePath 要删除文件的路径
     * @throws SftpException
     */
    public boolean delete(String filePath) throws SftpException {
        sftp.cd(SEPARATOR);
        String now = sftp.pwd();
        sftp.cd(getDirectory(filePath));
        sftp.rm(getFileName(filePath));
        sftp.cd(now);
        return true;
    }


    /**
     * @功能描述 从路径中抽取文件名
     * @param path 路径
     * @return 返回文件名
     */
    private String getFileName(String path) {
        try {
            if (path.contains("\\")) {
                return path.substring(path.lastIndexOf("\\") + 1);
            }else if(path.contains("/")){
                return path.substring(path.lastIndexOf("/") + 1);
            }else{
                return path;
            }
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * @功能描述 从路径中抽取目录
     * @return 返回目录
     */
    private String getDirectory(String filePath) {
        try {
            if (filePath.contains("\\")) {
                return filePath.substring(0, filePath.lastIndexOf("\\") + 1);
            }
            return new File(filePath).getPath().substring(0, filePath.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 获取remotePath路径下以regex格式指定的文件列表,传""或者null默认获取所有文件
     * @param remotePath sftp服务器上的目录
     * @param regex 需要匹配的文件名，
     * @return 获取的文件列表
     * @throws SftpException
     */
    @SuppressWarnings("unchecked")
    public List<String> listFiles(String remotePath, String regex) throws SftpException {
        List<String> fileList = new ArrayList<String>();
        try{
            // 如果remotePath不是目录则会抛出异常
            sftp.cd(remotePath);
            if ("".equals(regex) || regex == null) {
                regex = "*";
            }
            Vector<LsEntry> sftpFiles = sftp.ls(regex);
            String fileName = null;
            for (LsEntry lsEntry : sftpFiles) {
                fileName = lsEntry.getFilename();
                fileList.add(fileName);
            }
        }catch (Exception e) {
            LOGGER.error("get file list from path :"+remotePath + " exception "+e.getMessage(), e);
            fileList = null;
        }
        return fileList;
    }



    /**
     * 根据用户名，主机ip，端口获取一个Session对象
     * @param jsch jsch对象
     * @param host 主机ip
     * @param username 用户名
     * @param port 端口
     * @return
     * @throws Exception
     */
    private Session createSession(JSch jsch, String host, String username,int port) throws Exception {
        Session session = null;
        if (port <= 0) {
// 连接服务器，采用默认端口
            session = jsch.getSession(username, host);
        } else {
// 采用指定的端口连接服务器
            session = jsch.getSession(username, host, port);
        }
// 如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new Exception(host + "session is null");
        }
// 设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    /**
     * 循环创建远程路径
     * @param destDirPath
     * @throws SftpException
     */
    private void createRemotedir(String destDirPath) throws SftpException {
//设置根路径
        sftp.cd(SEPARATOR);
        String[] folders = destDirPath.split(SEPARATOR);
        for (String folder : folders) {
            if (folder.length() > 0) {
                try {
// 如果folder不存在，则会报错，此时捕获异常并创建folder路径
                    sftp.cd(folder);
                } catch (SftpException e) {
                    sftp.mkdir(folder);
                    sftp.cd(folder);
                }
            }
        }
    }
    /**
     * 创建本地路径
     * @param savePath 本地文件路径
     * @return
     * @throws Exception
     */
    private File createLocalDir(String savePath) throws Exception {
        File localPath = new File(savePath);
        if (!localPath.exists() && !localPath.isFile()) {
            if (!localPath.mkdirs()) {
                throw new Exception(localPath + " directory can not create. ");
            }
        }
        return localPath;
    }




    /**
     * Disconnect with server
     * 断开sftp连接
     */
    public void disconnect() {
        try {
            if (sftp != null) {
                sftp.quit();
            }
            if (sftp != null) {
                sftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        } catch (Exception e) {
            sftp = null;
            session = null;
        }
    }

    private String setDirectoryFile(String path, String fileName) {
        return path + File.pathSeparator + fileName;
    }

    public void close() throws Exception {
        sftp.disconnect();
    }
}
