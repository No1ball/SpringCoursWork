package com.example.springtest.repos;

import com.example.springtest.entity.ContractsSqlDao;
import com.example.springtest.entity.DevicesSqlDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ContractsRepo extends CrudRepository<ContractsSqlDao, Integer> {
    List<ContractsSqlDao> findByCompNameContainingIgnoreCaseOrderByCompName(String name);
    List <ContractsSqlDao> findAll();

}