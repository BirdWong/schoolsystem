package cn.jsuacm.ccw.mapper.blog;

import cn.jsuacm.ccw.pojo.blog.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @ClassName CategoryMapper
 * @Description TODO
 * @Author h4795
 * @Date 2019/06/21 17:34
 */
@Component
@Mapper
public interface CategoryMapper extends BaseMapper<Category>{
}
