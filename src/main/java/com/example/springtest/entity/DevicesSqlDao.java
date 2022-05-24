package com.example.springtest.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "device")
public class DevicesSqlDao {
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private int price;
    @Column(name = "countSale")
    private int countSale = 0;
    @Column(name = "totalSumm")
    private int totalSumm = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddevice")
    private int id;


    @ManyToMany(mappedBy = "equipments")
    private List<ContractsSqlDao> contract;

    public boolean setId(int new_id) {
        this.id = new_id;
        return true;
    }
    public boolean setContract(List<ContractsSqlDao> new_contract){
        this.contract = new_contract;
        return true;
    }

    public boolean setName(String new_name) { //prototype
        this.name = new_name;
        return true;
    }

    public boolean setPrice(int new_price){ //prototype
        this.price = new_price;
        return true;
    }

    public boolean setCountSale(int new_cntSale){ //prototype
        this.countSale = new_cntSale;
        return true;
    }

    public boolean setTotalSumm(int new_totalsumm){ //prototype
        this.totalSumm = new_totalsumm;
        return true;
    }

    public List<ContractsSqlDao> getContract(){
        return this.contract;
    }

    public String getName(){
        return this.name;
    }

    public int getPrice(){
        return this.price;
    }

    public int getCountSale(){
        return this.countSale;
    }

    public int getTotalSumm(){
        return this.totalSumm;
    }

    public int getId() {
        return this.id;
    }
}

