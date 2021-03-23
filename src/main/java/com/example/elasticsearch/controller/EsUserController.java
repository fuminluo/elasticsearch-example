package com.example.elasticsearch.controller;

import com.example.elasticsearch.entity.ESUser;
import com.example.elasticsearch.repository.EsUserRepository;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author LuoFuMin
 * @DATE 2021/3/15 16:03
 */
@RestController
@RequestMapping("/es/user")
public class EsUserController {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private EsUserRepository esUserRepository;

    @RequestMapping(value = "/create-index", method = RequestMethod.POST)
    public Object createEsIndex() {
        boolean index = elasticsearchRestTemplate.createIndex(ESUser.class);
        elasticsearchRestTemplate.putMapping(ESUser.class);
        System.out.println("创建索引结果是" + index);
        return index;
    }

    @RequestMapping(value = "/delete-index", method = RequestMethod.POST)
    public Object deleteEsIndex() {
        boolean deleteIndex = elasticsearchRestTemplate.deleteIndex(ESUser.class);
        System.out.println("删除索引结果是" + deleteIndex);
        return deleteIndex;
    }

    @RequestMapping(value = "/exist-index", method = RequestMethod.POST)
    public Object existEsIndex() {
        boolean existsIndex = elasticsearchRestTemplate.indexExists(ESUser.class);
        System.out.println("是否存在的结果是" + existsIndex);
        return existsIndex;
    }

    @RequestMapping(value = "/save-doc", method = RequestMethod.POST)
    public ESUser saveEsDoc(@RequestBody ESUser esUser) {
        ESUser result = esUserRepository.save(esUser);
        return result;
    }

    @RequestMapping(value = "/query-doc", method = RequestMethod.GET)
    public List<ESUser> queryByName(String name) {
        List<ESUser> result = esUserRepository.queryESUserByName(name);
        return result;
    }

    @RequestMapping(value = "/query-all", method = RequestMethod.GET)
    public Object query() {
        return esUserRepository.findAll();
    }

    @RequestMapping(value = "/exist-doc", method = RequestMethod.GET)
    public Object existDoc(Long id) {
        return esUserRepository.existsById(id);
    }


    //---------------- 复杂查询 ------------------
    @RequestMapping(value = "/query-doc/complex", method = RequestMethod.POST)
    public Object queryByName(@RequestBody ESUser esUser) {
        String desc = esUser.getDesc();
        List<String> tags = esUser.getTags();
        String name = esUser.getName();
        // 先构建查询条件
        BoolQueryBuilder defaultQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(desc)) {
            defaultQueryBuilder.should(QueryBuilders.termQuery("desc", desc));
        }
        if (StringUtils.isNotBlank(name)) {
            defaultQueryBuilder.should(QueryBuilders.termQuery("name", name));
        }
        if (!CollectionUtils.isEmpty(tags)) {
            for (String tag : tags) {
                defaultQueryBuilder.must(QueryBuilders.termQuery("tags", tag));
            }
        }

        // 分页条件
        PageRequest pageRequest = PageRequest.of(0, 10);
        // 高亮条件
        HighlightBuilder highlightBuilder = getHighlightBuilder("desc", "tags");
        // 排序条件
        FieldSortBuilder sortBuilder = SortBuilders.fieldSort("age").order(SortOrder.DESC);
        //组装条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(defaultQueryBuilder)
                .withHighlightBuilder(highlightBuilder)
                .withPageable(pageRequest)
                .withSort(sortBuilder)
                .build();

        SearchHits<ESUser> searchHits = elasticsearchRestTemplate.search(searchQuery, ESUser.class);

        // 高亮字段映射
       /* List<EsUserVo> userVoList = Lists.newArrayList();
        for (SearchHit<ESUser> searchHit : searchHits) {
            ESUser content = searchHit.getContent();
            ESUserVo esUserVo = new ESUserVo();
            BeanUtils.copyProperties(content,esUserVo);
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (String highlightField : highlightFields.keySet()) {
                if (StringUtils.equals(highlightField,"tags")){
                    esUserVo.setTags(highlightFields.get(highlightField));
                }else if(StringUtils.equals(highlightField,"desc")){
                    esUserVo.setDesc(highlightFields.get(highlightField).get(0));
                }

            }
            userVoList.add(esUserVo);
        }

        // 组装分页对象
        Page<ESUserVo> userPage = new PageImpl<>(userVoList,pageRequest,searchHits.getTotalHits());*/

        return searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    // 设置高亮字段
    private HighlightBuilder getHighlightBuilder(String... fields) {
        // 高亮条件
        HighlightBuilder highlightBuilder = new HighlightBuilder(); //生成高亮查询器
        for (String field : fields) {
            highlightBuilder.field(field);//高亮查询字段
        }
        highlightBuilder.requireFieldMatch(false);     //如果要多个字段高亮,这项要为false
        highlightBuilder.preTags("<span style=\"color:red\">");   //高亮设置
        highlightBuilder.postTags("</span>");
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(800000); //最大高亮分片数
        highlightBuilder.numOfFragments(0); //从第一个分片获取高亮片段

        return highlightBuilder;
    }

}
