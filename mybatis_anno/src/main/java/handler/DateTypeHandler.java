package handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeHandler extends BaseTypeHandler<Date> {
    //将java类型转换成数据库需要的类型
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Date date, JdbcType jdbcType) throws SQLException {
        long time = date.getTime();
        preparedStatement.setLong(i,time);
    }


    //三个get都是将数据库中的类型转换成java类型
    //String 要转换的字段名称
    //ResultSet结果集
    @Override
    public Date getNullableResult(ResultSet resultSet, String s) throws SQLException {
        long aLong;
        String str;
        Date date = null;
        try{
            aLong = resultSet.getLong(s);
            date = new Date(aLong);
        }catch (Exception e){
            str = resultSet.getString(s);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = format.parse(str);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        return date;
    }


    @Override
    public Date getNullableResult(ResultSet resultSet, int i) throws SQLException {
        long aLong;
        String str;
        Date date = null;
        try{
            aLong = resultSet.getLong(i);
            date = new Date(aLong);
        }catch (Exception e){
            str = resultSet.getString(i);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = format.parse(str);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        return date;
    }

    @Override
    public Date getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        long aLong;
        String str;
        Date date = null;
        try{
            aLong = callableStatement.getLong(i);
            date = new Date(aLong);
        }catch (Exception e){
            str = callableStatement.getString(i);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = format.parse(str);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        return date;
    }
}
