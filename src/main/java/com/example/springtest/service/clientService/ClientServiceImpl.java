package com.example.springtest.service.clientService;

import com.example.springtest.entity.ClientsSqlDao;
import com.example.springtest.entity.ContractsSqlDao;
import com.example.springtest.entity.DevicesSqlDao;
import com.example.springtest.repos.ClientsRepo;
import com.example.springtest.repos.ContractsRepo;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.swing.*;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService{
    @Autowired
    private ClientsRepo clientsRepo;
    private static final Logger log = Logger.getLogger("ClientServiceImpl.class");
    @Autowired
    private ContractsRepo contractsRepo;
    public static final String FONT = "./src/main/resources/arialmt.ttf";
    @Override
    public List<ClientsSqlDao> getCompany(){
        log.info("Список клиентов");
        List<ClientsSqlDao> fullList = clientsRepo.findAll();
        List<ClientsSqlDao> listComp = fullList.stream().filter(element->(element.getTotalSumm()>=0)).collect(Collectors.toList());
        return listComp;
    }
    @Override
    public void delCompany(int id){
        log.info("Удаление клиента");
        ClientsSqlDao client = clientsRepo.findById(id).orElseThrow();
        ContractsSqlDao clientContr = client.getContractId();
        if(clientContr != null) {
            List<DevicesSqlDao> devises = clientContr.getEquipments();
            for (DevicesSqlDao eqiup : devises) {
                eqiup.delOneCntr(clientContr);
                eqiup.setNwStr();
            }
            clientContr.setEquipments(null);
        }
        List<ContractsSqlDao> oldCntr = client.getOldContracts();
        if(oldCntr!=null){
            for (ContractsSqlDao iContr: oldCntr) {
                for (DevicesSqlDao eqiup: iContr.getEquipments()) {
                    eqiup.delOneCntr(iContr);
                    eqiup.setNwStr();
                }
                iContr.setEquipments(null);
            }
        }
        clientsRepo.deleteById(id);
    }
    @Override
    public ClientsSqlDao putCompany(int id, ClientsSqlDao company){
        log.info("Изменения клиента");
        ClientsSqlDao comp = clientsRepo.findById(id).orElseThrow();
        ContractsSqlDao contract;
        comp.setName(company.getName());
        comp.setContact(company.getContact());
        if(company.getNum()!= 0){
            if(comp.getNum() != 0){
                contract = contractsRepo.findById(comp.getNum()).orElseThrow();
                contract.setClient(null);
            }
            contract = contractsRepo.findById(company.getNum()).orElseThrow();
            contract.setClient(comp);
            comp.setContractId(contract);
            comp.setNum(contract.getId());
            contract.setCompName(comp.getName());
            comp.setTotalSumm(comp.getTotalSumm()+contract.getPrice());
        }
        if(comp.getOldContracts()!=null){
            List<ContractsSqlDao> oldContr = comp.getOldContracts();
            String neName = company.getName();
            oldContr.forEach(e->e.setNewCompName(neName));
        }
        return clientsRepo.save(comp);
    }
    @Override
    public Iterable<ClientsSqlDao> toOld(int id){
        log.info("Перевод");
        ClientsSqlDao company = clientsRepo.findById(id).orElseThrow();
        company.addOldContract(company.getContractId());
        ContractsSqlDao cont = contractsRepo.findById(company.getContractId().getId()).orElseThrow();
        cont.setClient(null);
        cont.setOldClient(company);
        company.setContractId(null);
        company.setNum(0);
        List<ClientsSqlDao> clie = Arrays.asList(company);
        return clientsRepo.saveAll(clie);
    }

    @Override
    public Iterable<ClientsSqlDao> noContractId(int id){
        log.info("Отвязывание клиента от контракта");
        ClientsSqlDao clie = clientsRepo.findById(id).orElseThrow();
        ContractsSqlDao contr = clie.getContractId();
        clie.setTotalSumm(clie.getTotalSumm()-contr.getPrice());
        contr.setClient(null);
        clie.setContractId(null);
        clie.setNum(0);
        return clientsRepo.saveAll(Arrays.asList(clie));
    }


    @Override
    public Iterable<ClientsSqlDao> addCompany(ClientsSqlDao company){
        log.info("Добавление компании");
        ContractsSqlDao contract = new ContractsSqlDao();
        if(company.getNum() != 0){
            try{
                contract = contractsRepo.findById(company.getNum()).orElseThrow();
                contract.setClient(company);
                contract.setCompName(company.getName());
            }catch (Exception ex) {
                contract.setId(company.getNum());
                contract.setCompName(company.getName());
                contract.setLDate(new Date());
                contract.setFDate(new Date());
                contract.setPrice();
                contractsRepo.save(contract);
            }
            company.setContractId(contract);
            contract.setClient(company);
            company.setTotalSumm(contract.getPrice());
        }else{
            company.setTotalSumm(0);
        }
        List<ClientsSqlDao> clie = Arrays.asList(company);
        return clientsRepo.saveAll(clie);
    }

    @Override
    public List<ClientsSqlDao> topClient(String name){
        log.info("Топ клиентов");
        List<ClientsSqlDao> client = clientsRepo.findByNameContainsIgnoreCaseOrderByName(name);
        List<ClientsSqlDao> listComp = client.stream().filter(element->(element.getTotalSumm()>=0)).collect(Collectors.toList());
        return listComp.stream().sorted(Comparator.comparingInt(ClientsSqlDao::getTotalSumm).reversed()).collect(Collectors.toList());
    }
    @Override
    public List<ClientsSqlDao> searchCompany(String name){
        log.info("Поиск клиентов по имени");
        List<ClientsSqlDao> client = clientsRepo.findByNameContainsIgnoreCaseOrderByName(name);
        return client.stream().filter(element->(element.getTotalSumm()>=0)).collect(Collectors.toList());
    }
    @Override
    public void toPdf(List list, int status){
        log.info("Генерация pdf Отчета");
        Document document = new Document();
        try {
            BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 14, Font.NORMAL);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\asus\\Desktop\\Отчеты\\lab7\\AddTableClients.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(6); // 6 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            table.setWidths(new int[]{15,50,20,35,35,90});
            PdfPCell cell0 = new PdfPCell(new Paragraph("№"));
            PdfPCell cell1 = new PdfPCell(new Paragraph("Name"));

            cell0.setPaddingLeft(10);
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell1.setPaddingLeft(10);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell2 = new PdfPCell(new Paragraph("Contact"));

            cell2.setPaddingLeft(10);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell3 = new PdfPCell(new Paragraph("ContractID"));

            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell4 = new PdfPCell(new Paragraph("OldCntrctID"));
            cell4.setPaddingLeft(10);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell5 = new PdfPCell(new Paragraph("Total Summ"));
            cell5.setPaddingLeft(10);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);


            table.addCell(cell0);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            Iterable<ClientsSqlDao> dataset1 = clientsRepo.findAllById(list);
            List<ClientsSqlDao> dataset = new ArrayList<>();
            dataset1.forEach(dataset::add);
            if(status == 1){
                dataset = dataset.stream().sorted(Comparator.comparingInt(ClientsSqlDao::getTotalSumm).reversed()).collect(Collectors.toList());
            }
            //Set Column widths
            int i = 1;
            for (ClientsSqlDao record : dataset) {
                String nStr = "";
                table.addCell(String.valueOf(i));
                table.addCell(new Paragraph(record.getName(), font));
                table.addCell(new Paragraph(record.getContact(), font));
                table.addCell(String.valueOf(record.getContractId().getId()));
                for (ContractsSqlDao devic : record.getOldContracts()) {
                    nStr = nStr.concat("Id : ").concat(String.valueOf(devic.getId())).concat("\n");
                }
                table.addCell(new Paragraph(nStr, font));
                table.addCell(String.valueOf(record.getTotalSumm()));
                i++;
            }
            document.add(table);

            document.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}