package org.Sql;

public interface userFindSQL
{
    public boolean userIsExist(String username , String password);

    public boolean userNameExists(String username);
}
