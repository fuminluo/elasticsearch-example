package com.example.elasticsearch.repository;

import com.example.elasticsearch.entity.ESUser;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author LuoFuMin
 * @DATE 2021/3/15 16:00
 */
@ResponseBody
public interface EsUserRepository extends ElasticsearchRepository<ESUser, Long> {

    long deleteESUserByName(String name);

    List<ESUser> queryESUserByName(String name);
}
