package cn.linguaverse.course.mapper;

import cn.linguaverse.course.entity.LessonVocab;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LessonVocabMapper extends BaseMapper<LessonVocab> {

    /** 按语种 + 单词反查课程词汇（用于 SRS 首次评分时校验与补全音标/释义）。 */
    @Select("SELECT lv.* FROM lesson_vocab lv "
            + "JOIN lessons l ON l.id = lv.lesson_id "
            + "WHERE l.lang_code = #{langCode} AND lv.word = #{word} LIMIT 1")
    LessonVocab findInLang(@Param("langCode") String langCode, @Param("word") String word);
}
