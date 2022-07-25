package domain;

import com.sun.istack.internal.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class Order {
    private int id;
    private Date orderTime;
    private double total;
    //订单属于的用户
    private User user;
}
