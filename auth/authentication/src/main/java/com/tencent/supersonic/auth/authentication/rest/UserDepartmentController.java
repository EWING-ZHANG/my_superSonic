package com.tencent.supersonic.auth.authentication.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentResp;
import com.tencent.supersonic.auth.authentication.pojo.Organization;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
import com.tencent.supersonic.auth.authentication.request.UserWithDepartmentPageReq;
import com.tencent.supersonic.auth.authentication.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/api/semantic/department")
public class UserDepartmentController {
    @Autowired
    private UserDepartmentRepository userDepartmentService;
    @Autowired
    private DepartmentService departmentService;

    @PostMapping("/saveOrUpdateDepartmentForUser")
    public Boolean addDepartmentForUser(@RequestBody UserDepartmentReq userDepartmentReq)
            throws InvocationTargetException, IllegalAccessException {
        userDepartmentService.addUserDepartment(userDepartmentReq);
        return true;
    }

    /**
     * 添加部门
     * 
     * @param departmentReq
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public Boolean saveOrUpdate(@RequestBody DepartmentReq departmentReq) {
        //多选 一个用户可以添加多个部门
        return departmentService.SaveOrUpdate(departmentReq);
    }
    /**
     * 新增 一个用户可以添加多个部门 存一个list
     */
    @PostMapping("/saveOrUpdateList")
    public Boolean saveOrUpdateList(@RequestBody List<UserDepartmentDO> userDepartmentDOS) {
        //多选 一个用户可以添加多个部门
        return userDepartmentService.saveOrUpdateUserList(userDepartmentDOS);
    }


    /**
     *  共用相关的接口
     */
    @PostMapping("/saveOrUpdateLiDepartmentList")
    public Boolean saveOrUpdateLiDepartmentList(@RequestBody List<UserDepartmentDO> userDepartmentDOS) {
        //多选 一个用户可以添加多个部门
        return userDepartmentService.saveOrUpdateList(userDepartmentDOS);
    }
    /**
     * 获取所有部门信息 有从属关系的 先直接获取所有的部门吧|
     * 
     * @return
     */
    @GetMapping("getDepartmentList")
    public List<DepartmentDO> getDepartmentList() {
        return departmentService.getDepartmentList();
    }

    /**
     * 获取没有添加部门信息的用户
     * 
     * @return
     */
    @GetMapping("getUserWithoutDepartment")
    public List<UserDepartmentDO> getUserWithoutDepartment() {
        return userDepartmentService.getUserWithoutDepartment();
    }

    /**
     * 只用到了这个接口返回所有用户（分页版） 展示数据成部门groupby的形式
     * @return
     */

    @PostMapping("/getUserWithDepartment")
    public IPage<UserDepartmentResp> getUserWithDepartment(@RequestBody UserWithDepartmentPageReq req) {

        return userDepartmentService.getUserWithDepartment(req.getPageNum(), req.getPageSize(), req.getDisplayName(),req.getDepartmentName(),req.getDepartmentIds());
    }

    // 根据用户名称进行模糊查询用户以及部门信息数据
    @GetMapping("searchByName")
    public List<UserDepartmentDO> searchByName(@RequestParam String searchName) {
        return userDepartmentService.searchByName(searchName);
    }

    // 获取organizationTree
    @GetMapping("getOrganizationTree")
    public List<Organization> getOrganizationTree() {
        return departmentService.getOrganizationTree();
    }

    @PostMapping("/addDepartment")
    public void addDepartment(@RequestBody DepartmentReq req)
            throws InvocationTargetException, IllegalAccessException {
        departmentService.addDepartment(req);
    }
    /**
     * 删除部门
     * @param id
     * @return
     */
    @GetMapping("/deleteDepartmentById/{id}")
    public void deleteDepartmentById(@PathVariable Long id) {
        departmentService.deleteDepartmentAndSubById(id);
    }

    /**
     * 查询部门下有哪些用户
     * @param id
     * @return
     */
    @GetMapping("/getUserListByDepartmentId/{id}")
    public List<UserDepartmentDO> getUserListByDepartmentId(@PathVariable Long id) {
        return userDepartmentService.getUserListByDepartmentId(id);
    }
    @PutMapping("/updateDepartmentName")
    public void updateDepartmentName(@RequestBody DepartmentReq req) {
        departmentService.updateDepartmentName(req);
    }

}
