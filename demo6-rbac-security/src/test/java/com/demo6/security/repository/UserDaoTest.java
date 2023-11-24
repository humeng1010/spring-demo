package com.demo6.security.repository;

import com.demo6.security.SpringBootDemoRbacSecurityApplicationTests;
import com.demo6.security.model.User;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * UserDao 测试
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-12 01:10
 */
@Slf4j
public class UserDaoTest extends SpringBootDemoRbacSecurityApplicationTests {
    @Autowired
    private UserDao userDao;

    @Test
    public void findByUsernameIn() {
        List<String> usernameList = Lists.newArrayList("admin", "user");
        List<User> userList = userDao.findByUsernameIn(usernameList);
        Assert.assertEquals(2, userList.size());
        log.info("【userList】= {}", userList);
    }
}
