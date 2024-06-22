package org.Sql;

import java.util.List;
import java.util.Map;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 14:02
 * @Description: TODO
 * @version: 1.0
 **/


public interface userFindSQL
{

    /**
     * @param null
     * @description: a
     * @param:
     * @return:
     * @author Edge
     * @date: 2024/6/22 14:02
     **/

    // 通过账户密码判断用户是否存在
    public boolean userIsExist(String username , String password);

    // 判断用户名是否存在
    public boolean userNameExists(String username);

    // 通过用户名获取用户信息
    public Map<String, Object> getUserInfo(String username);

    // 通过用户名获取好友列表
    public List<Map<String, Object>> getFriendList(String username);
}
