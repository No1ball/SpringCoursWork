package com.example.springtest.service.companyService;
import com.example.springtest.entity.ClientsSqlDao;
import com.example.springtest.entity.ContractsSqlDao;
import com.example.springtest.repos.ClientsRepo;
import com.example.springtest.repos.ContractsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService{
    @Autowired
    private ClientsRepo companyRepo;

    @Autowired
    private ContractsRepo contractsRepo;

    @Override
    public List<ClientsSqlDao> getCompany(){
        List<ClientsSqlDao> fullList = companyRepo.findAll();
        List<ClientsSqlDao> listComp = fullList.stream().filter(element->(element.getTotalSumm()<0)).collect(Collectors.toList());
        return listComp;
    }
    @Override
    public void delCompany(int id){
        companyRepo.deleteById(id);
    }
    @Override
    public ClientsSqlDao putCompany(int id, ClientsSqlDao company){
        ClientsSqlDao comp = companyRepo.findById(id).orElseThrow();
        comp.setName(company.getName());
        comp.setContact(company.getContact());
        return companyRepo.save(comp);
    }
    @Override
    public ClientsSqlDao addCompany(ClientsSqlDao company){
        return companyRepo.save(company);
    }
    @Override
    public Iterable<ClientsSqlDao> toClient(int id, ClientsSqlDao client){
        ClientsSqlDao clien = companyRepo.findById(id).orElseThrow();
        ContractsSqlDao contract;
        try{
            contract = contractsRepo.findById(client.getNum()).orElseThrow();
            contract.setClient(client);
            contract.setCompName(clien.getName());
        }catch (Exception ex){
            contract = new ContractsSqlDao();
            contract.setId(client.getNum());
            contract.setCompName(client.getName());
            contract.setLDate(new Date());
            contract.setFDate(new Date());
            contract.setPrice();
            contractsRepo.save(contract);
        }
        clien.setContractId(contract);
        if(clien.getContractId()==null){
            clien.setTotalSumm(0);
        }else{
            clien.setTotalSumm(clien.getContractId().getPrice());
        }
        clien.setNum(clien.getContractId().getId());
        List<ClientsSqlDao> clie = Arrays.asList(clien);
        return companyRepo.saveAll(clie);
    }
    @Override
    public List<ClientsSqlDao> searchCompany(String name){
        List<ClientsSqlDao> fullList = companyRepo.findByNameContainsIgnoreCaseOrderByName(name);
        return fullList.stream().filter(element->(element.getTotalSumm()<0)).collect(Collectors.toList());
    }
    @Override
    public void toPdf(String name){

    }
}