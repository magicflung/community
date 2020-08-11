package com.community.controller;

import com.community.annotation.LoginRequired;
import com.community.entity.User;
import com.community.service.FollowService;
import com.community.service.LikeService;
import com.community.service.UserService;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author flunggg
 * @date 2020/7/24 22:22
 * @Email: chaste86@163.com
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    // 看看当前是哪一个用户
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    // 引入七牛云
    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;


    /**
     * @return 跳转到用户个人设置页面
     * LoginRequired:只是一个标记
     */
    @GetMapping("/setting")
    public String getSettingPage(Model model) {

        // 重构：引入七牛云
        // 1. 上传文件名称(每次都变，云服务器会缓存，一旦变化就可以响应，不然会因为缓存而不能立即改变头像)
        String fileName = CommunityUtil.generateUUID();
        // 2. 设置响应信息（七牛云提供的，异步请求）
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(200));
        // 3. 生成上传的凭证
        Auth auth = Auth.create(accessKey, secretKey);
        // 过期时间；3600s
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    /**
     * 个人主页，可以看自己的也可以看别人的
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        // 把用户信息发给页面
        model.addAttribute("user", user);
        // 用户收到的赞
        int likeCount = likeService.findEntityUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否关注(自己看自己就没有显示，这在前端实现)
        boolean hasFollowed = false;
        // 看看是否登录
        if(hostHolder.getUser() != null) {
            // 当前用户有没有关注Ta
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    /**
     * 引入七牛云后，在上传头像后更新表中的头像路径
     * @param fileName 头像名
     * @return
     */
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if(StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(403, "文件名不能为空");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(200, "更新头像成功");
    }

    // /** 引入七牛云，废弃下面的上传方法
    //  * 上传文件
    //  * @param headerImage 前端发送来的图片
    //  * @param model 传递给前端的数据
    //  * @return
    //  */
    // @PostMapping("/upload")
    // public String settingHeader(MultipartFile headerImage, Model model) {
    //     if(headerImage == null) {
    //         model.addAttribute("error", "您还没有上传文件");
    //         return "/site/setting";
    //     }
    //     String fileName = headerImage.getOriginalFilename();
    //     String suffix = fileName.substring(fileName.lastIndexOf("."));
    //     if(StringUtils.isBlank(suffix)) {
    //         model.addAttribute("error", "您上传的文件格式不对");
    //         return "/site/setting";
    //     }
    //     // 最好给用户上传的文件重新以随机字符串命名
    //     fileName = CommunityUtil.generateUUID() + suffix;
    //     // 创建一个文件，表示要保存在哪里（本地存储）
    //     File file = new File(uploadPath + fileName);
    //     if(!file.isDirectory()) {
    //         file.mkdirs();
    //     }
    //     // 把内容读入到file
    //     try {
    //         headerImage.transferTo(file);
    //     } catch (IOException e) {
    //         logger.error("上传文件失败：" + e.getMessage());
    //         throw new RuntimeException("上传文件失败：", e);
    //     }
    //
    //     // 更新当前用户的投降（以web路径）
    //     // http:8080/community/user/header/xxx.jpg
    //     // 因为得被动访问，不是主动请求，所以拼上域名和项目名
    //     String headerUrl = domain + contextPath + "/user/header/" + fileName;
    //     // 获取当前用户
    //     User user = hostHolder.getUser();
    //     // 更新
    //     userService.updateHeader(user.getId(), headerUrl);
    //
    //     return "redirect:/index";
    // }


    // /** 引入七牛云，废弃下面的获取头像方法
    //  * 会根据url获取用户头像：http:8080/community/user/header/xxx.jpg
    //  * 会在用户头像那里设置动态的src，使之可以自动调用getHeader方法
    //  * @param fileName 用户的头像文件名
    //  * @param response
    //  */
    // @GetMapping("/header/{fileName}")
    // public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
    //     // 图片在服务器存放的位置
    //     fileName = uploadPath + fileName;
    //     // 获取文件后缀
    //     String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
    //     // 获取文件必要的响应头
    //     response.setContentType("image/" + suffix);
    //     // 写个IO，把二进制图片放到网址上去，以字节流的形式
    //     try(  OutputStream outputStream = response.getOutputStream();
    //           FileInputStream inputStream = new FileInputStream(fileName);) {
    //         byte[] buffer = new byte[1024];
    //         int b;
    //         while((b = inputStream.read(buffer)) != -1) {
    //             outputStream.write(buffer, 0, b);
    //         }
    //     } catch (IOException e) {
    //         logger.error("文件读取失败：" + e.getMessage());
    //     }
    // }

    /**
     * 修改密码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/password")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        if(StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oerror", "原密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)) {
            model.addAttribute("nerror", "新密码不能为空");
            return "/site/setting";
        }
        // 取出当前用户
        User user = hostHolder.getUser();
        // 比较旧密码是否与当前用户的密码匹配
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!oldPassword.equals(user.getPassword())) {
            model.addAttribute("oerror", "输入的原密码错误");
            return "/site/setting";
        }
        // 更新
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/index";
    }
}
