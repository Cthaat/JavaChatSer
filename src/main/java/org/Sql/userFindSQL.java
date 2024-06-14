package org.Sql;

import java.util.Map;

public interface userFindSQL
{
    public boolean userIsExist(String username , String password);

    public boolean userNameExists(String username);

    public Map<String , Object> getUserInfo(String username);
}
