package com.example.springtest.controllers;
import com.example.springtest.entity.ClientsSqlDao;
import com.example.springtest.repos.ClientsRepo;
import com.example.springtest.service.clientService.ClientService;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class clientsController {
    @Autowired
    private ClientService clientsService;
    @PostMapping ("/add_clients")
    public ResponseEntity saveClient(@RequestBody ClientsSqlDao client){
        return ResponseEntity.ok(clientsService.addCompany(client));
    }
    @DeleteMapping("/client/del/onlyContractId/{id}")
    public ResponseEntity on1(@PathVariable("id") int id){
        return ResponseEntity.ok(clientsService.noContractId(id));
    }
    @PutMapping("/client/toOld/{id}")
    public ResponseEntity toOld(@PathVariable("id") int id){
        return ResponseEntity.ok(clientsService.toOld(id));
    }
    @GetMapping("/client")
    public ResponseEntity getClient(){
        return ResponseEntity.ok(clientsService.getCompany());
    }
    @GetMapping("/clients")
    public String viewClient(){
        return "clients";
    }
    @GetMapping("/client/topSumm")
    public ResponseEntity topClient(@RequestParam("name") String name){
        return ResponseEntity.ok(clientsService.topClient(name));
    }
    @DeleteMapping("/client/del/{id}")
    public String deleteClient(@PathVariable("id") int id){
        clientsService.delCompany(id);
        return "clients";
    }
    @GetMapping("/client1")
    public ResponseEntity search(@RequestParam("name") String name){
        return ResponseEntity.ok(clientsService.searchCompany(name));
    }
    @PutMapping("/client/ed/{id}")
    public ResponseEntity editClient(@PathVariable("id") int id, @RequestBody ClientsSqlDao client){
        return ResponseEntity.ok(clientsService.putCompany(id, client));
    }
    @GetMapping("/client/topdf/{list}")
    public String pdfClient(@PathVariable("list") String list, @RequestParam("status") int status){
        System.out.println(list);
        String[] array = list.split(",");
        List<Integer> intsList = new ArrayList<Integer>(array.length);
        for (int i = 0; i < array.length; i++) {
            intsList.add(i, Integer.parseInt(array[i]));
        }
        clientsService.toPdf(intsList, status);
        return "clients";
    }
}