package com.tencent.supersonic.auth.authentication.rest;

import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.pojo.Organization;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
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
        return departmentService.SaveOrUpdate(departmentReq);
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

    @GetMapping("getUserWithDepartment")
    public List<UserDepartmentDO> getUserWithDepartment() {
        return userDepartmentService.getUserWithDepartment();
    }
    //根据用户名称进行模糊查询用户以及部门信息数据
    @GetMapping("searchByName")
    public List<UserDepartmentDO> searchByName(@RequestParam String searchName){
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

    @GetMapping("/deleteDepartmentById/{id}")
    public void deleteDepartmentById(@PathVariable Long id) {
        departmentService.deleteDepartmentAndSubById(id);
    }


}
