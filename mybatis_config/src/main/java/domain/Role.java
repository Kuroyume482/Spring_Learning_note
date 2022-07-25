package domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Role {
    private int id;
    private String roleName;
    private String roleDesc;
}
