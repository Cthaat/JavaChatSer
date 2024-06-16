import com.fasterxml.jackson.databind.ObjectMapper;
import org.Sql.SQLUtils;
import org.Sql.p2pRoomChat;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class testSQL2
{
    @Test
    public void test1()
    {
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "delete from p2p_relationship where user_name = 'admin' and friend_name = 'testUser3'";
            int result = jdbcTemplate.update(sql);
            System.out.println(result);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test2()
    {
        p2pRoomChat p2p = new p2pRoomChat();
        p2p.loadMessagesFromTwoUsers("admin","testUser3");
    }
}

