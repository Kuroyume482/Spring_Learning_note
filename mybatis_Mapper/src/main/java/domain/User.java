package domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class User {
    private Integer id;
    private String username;
    private String password;
    private Date birthday;
}