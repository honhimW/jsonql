package io.github.honhimw.jsonql.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInfo implements Serializable {

    private String id;

    private JDBCDriverType driverType;

    private String host;

    private Integer port;

    private String username;

    private String password;

    private Map<String, Object> properties;

}
