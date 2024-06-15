package org.Sql;

import java.util.List;
import java.util.Map;

public interface userFindSQL
{
    // 通过账户密码判断用户是否存在
    public boolean userIsExist(String username , String password);
    // 判断用户名是否存在
    public boolean userNameExists(String username);
    // 通过用户名获取用户信息
    public Map<String , Object> getUserInfo(String username);
    // 通过用户名获取好友列表
    public List<Map<String, Object>> getFriendList(int userId);
}
