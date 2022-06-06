package com.example.springtest.service.companyService;
import com.example.springtest.entity.ClientsSqlDao;
import com.example.springtest.entity.ContractsSqlDao;
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

import java.io.FileOutputStream;
import java.util.*;

import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {
    @Autowired
    private ClientsRepo companyRepo;
    private static final Logger log = Logger.getLogger("CompanyServiceImpl.class");
    @Autowired
    private ContractsRepo contractsRepo;

    public static final String FONT = "./src/main/resources/arialmt.ttf";
    @Override
    public List<ClientsSqlDao> getCompany() {
        log.info("Список первичных контактов");
        List<ClientsSqlDao> fullList = companyRepo.findAll();
        List<ClientsSqlDao> listComp = fullList.stream().filter(element -> (element.getTotalSumm() < 0)).collect(Collectors.toList());
        return listComp;
    }

    @Override
    public void delCompany(int id) {
        log.info("Удаление первичного контакта");
        companyRepo.deleteById(id);
    }

    @Override
    public ClientsSqlDao putCompany(int id, ClientsSqlDao company) {
        log.info("Изменение первичного контакта");
        ClientsSqlDao comp = companyRepo.findById(id).orElseThrow();
        comp.setName(company.getName());
        comp.setContact(company.getContact());
        return companyRepo.save(comp);
    }

    @Override
    public ClientsSqlDao addCompany(ClientsSqlDao company) {
        log.info("Добавление первичного контакта");
        return companyRepo.save(company);
    }

    @Override
    public Iterable<ClientsSqlDao> toClient(int id, ClientsSqlDao client) {
        log.info("Перевод первичного контакта в статус клиента");
        ClientsSqlDao clien = companyRepo.findById(id).orElseThrow();
        ContractsSqlDao contract;
        try {
            contract = contractsRepo.findById(client.getNum()).orElseThrow();
            contract.setClient(client);
            contract.setCompName(clien.getName());

        } catch (Exception ex) {
            contract = new ContractsSqlDao();
            contract.setId(client.getNum());
            contract.setCompName(client.getName());
            contract.setLDate(new Date());
            contract.setFDate(new Date());
            contract.setPrice();
            contractsRepo.save(contract);
            log.info("Список клиентов");
        }
        clien.setContractId(contract);
        if (clien.getContractId() == null) {
            clien.setTotalSumm(0);
        } else {
            clien.setTotalSumm(clien.getContractId().getPrice());
        }
        clien.setNum(clien.getContractId().getId());
        List<ClientsSqlDao> clie = Arrays.asList(clien);
        return companyRepo.saveAll(clie);
    }

    @Override
    public List<ClientsSqlDao> searchCompany(String name) {
        log.info("Поиск первичного контакта по имени");
        List<ClientsSqlDao> fullList = companyRepo.findByNameContainsIgnoreCaseOrderByName(name);
        return fullList.stream().filter(element -> (element.getTotalSumm() < 0)).collect(Collectors.toList());
    }

    @Override
    public void toPdf(List list) {
        log.info("Генерация pdf отчета - первичные контакты");
        Document document = new Document();
        try {
            BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 14, Font.NORMAL);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\asus\\Desktop\\Отчеты\\lab7\\AddTableCompany.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(3); // 3 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            table.setWidths(new int[]{15, 50, 50});

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

            table.addCell(cell0);
            table.addCell(cell1);
            table.addCell(cell2);

            Iterable<ClientsSqlDao> dataset1 = companyRepo.findAllById(list);
            List<ClientsSqlDao> dataset = new ArrayList<>();
            dataset1.forEach(dataset::add);
            //Set Column widths
            int i = 1;
            for (ClientsSqlDao record : dataset) {
                table.addCell(String.valueOf(i));
                table.addCell(new Paragraph(record.getName(), font));
                table.addCell(new Paragraph(record.getContact(), font));
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
