package com.tencent.supersonic.auth.authentication.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tencent.supersonic.auth.api.authentication.pojo.Organization;
import com.tencent.supersonic.auth.api.authentication.pojo.UserToken;
import com.tencent.supersonic.auth.api.authentication.request.UserReq;
import com.tencent.supersonic.auth.api.authentication.request.UserTokenReq;
import com.tencent.supersonic.auth.api.authentication.service.UserService;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;
import com.tencent.supersonic.common.pojo.User;
import com.tencent.supersonic.common.pojo.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/user")
@Slf4j
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getCurrentUser")
    public User getCurrentUser(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        return userService.getCurrentUser(httpServletRequest, httpServletResponse);
    }

    @GetMapping("/getUserNames")
    public List<String> getUserNames() {
        return userService.getUserNames();
    }

    @GetMapping("/getUserList")
    public List<UserVO> getUserList() {
        //需要将用户的id都转换成字符串 否则前端无法识别 最后return为json格式
        List<User> userList = userService.getUserList();
        ArrayList<UserVO> resList = new ArrayList<>();
        if(userList != null) {
            for (User user : userList) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                userVO.setId(user.getId().toString());
                resList.add(userVO);
            }
            return resList;
        } else {
            return Collections.emptyList();
        }
    }

    @GetMapping("/getOrganizationTree")
    public List<Organization> getOrganizationTree() {
        return userService.getOrganizationTree();
    }

    @GetMapping("/getUserAllOrgId/{userName}")
    public Set<String> getUserAllOrgId(@PathVariable("userName") String userName) {
        return userService.getUserAllOrgId(userName);
    }

    @GetMapping("/getUserByOrg/{org}")
    public List<User> getUserByOrg(@PathVariable("org") String org) {
        return userService.getUserByOrg(org);
    }

    @PostMapping("/register")
    public void register(@RequestBody UserReq userCmd) {
        userService.register(userCmd);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserReq userCmd, HttpServletRequest request) {
        return userService.login(userCmd, request);
    }

    @PostMapping("/generateToken")
    public UserToken generateToken(@RequestBody UserTokenReq userTokenReq,
            HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getCurrentUser(request, response);
        return userService.generateToken(userTokenReq.getName(), user.getName(),
                userTokenReq.getExpireTime());
    }

    @GetMapping("/getUserTokens")
    public List<UserToken> getUserTokens(HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getCurrentUser(request, response);
        return userService.getUserTokens(user.getName());
    }

    @GetMapping("/getUserToken")
    public UserToken getUserToken(@RequestParam(name = "tokenId") Long tokenId) {
        return userService.getUserToken(tokenId);
    }

    @PostMapping("/deleteUserToken")
    public void deleteUserToken(@RequestParam(name = "tokenId") Long tokenId) {
        userService.deleteUserToken(tokenId);
    }

    /**
     * 删除用户
     * 
     * @param id
     */
    @DeleteMapping("/deleteUser/{id}")
    public void deleteUserById(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
    }

}
