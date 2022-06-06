package com.example.springtest.service.contractsService;

import com.example.springtest.entity.ClientsSqlDao;
import com.example.springtest.entity.ContractsSqlDao;
import com.example.springtest.entity.DevicesSqlDao;
import com.example.springtest.repos.ClientsRepo;
import com.example.springtest.repos.ContractsRepo;
import com.example.springtest.repos.DevicesRepo;
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
public class ContractsServiceImpl implements ContractsService{
    @Autowired
    private ContractsRepo contractsRepo;
    @Autowired
    private ClientsRepo clientsRepo;
    @Autowired
    private DevicesRepo devicesRepo;
    public static final String FONT = "./src/main/resources/arialmt.ttf";
    private static final Logger log = Logger.getLogger("ContractsServiceImpl.class");
    @Override
    public List<ContractsSqlDao> searchCompName(String name){
        log.info("Поиск контракта по имени клиента");
        return contractsRepo.findByCompNameContainingIgnoreCaseOrderByCompName(name);
    }
    @Override
    public Iterable<ContractsSqlDao> addDevice(ContractsSqlDao devices){
        log.info("Добавление контрактов");
        devices.setRelevance();
        if (devices.getTempStr()!=null) {
            String[] array = devices.getTempStr().split(",");
            List<Integer> intsList = new ArrayList<Integer>(array.length);
            for (int i = 0; i < array.length; i++) {
                intsList.add(i, Integer.parseInt(array[i]));
            }

            Iterable<DevicesSqlDao> cont = devicesRepo.findAllById(intsList);
            List<DevicesSqlDao> target = new ArrayList<>();
            cont.forEach(target::add);
            devices.setEquipments(target);
            for (int i = 0; i < target.size(); i++) {
                DevicesSqlDao tem = target.get(i);
                tem.setOneContract(devices);
                tem.setNwStr();
            }
            devices.setPrice();
        }
        List<ContractsSqlDao> dev = Arrays.asList(devices);
        return contractsRepo.saveAll(dev);
    }
    @Override
    public ContractsSqlDao viewId(int id){
        log.info("Нахождение контракта по id");
        return contractsRepo.findById(id).orElseThrow();
    }
    @Override
    public void delDev(int id){
        log.info("Удаление контракта");
        ContractsSqlDao contracts = contractsRepo.findById(id).orElseThrow();
        if(contracts.getClient()!= null){
            ClientsSqlDao client = contracts.getClient();
            client.setNum(0);
            client.setTotalSumm(client.getTotalSumm()-contracts.getPrice());
            client.setContractId(null);
            contracts.setClient(null);
            if(contracts.getEquipments()!=null){
                List<DevicesSqlDao> dev = contracts.getEquipments();
                for(int i = 0; i < dev.size(); i++){
                    DevicesSqlDao tem= dev.get(i);
                    tem.delOneCntr(contracts);
                    tem.setNwStr();
                    contracts.setPrice();
                }
                contracts.setEquipments(null);
            }
            clientsRepo.saveAll(Arrays.asList(client));

        }else if(contracts.getOldClient()!=null){
            ClientsSqlDao client = contracts.getOldClient();
            List<ContractsSqlDao> clientsCntr = client.getOldContracts();
            clientsCntr.remove(contracts);
            client.setTotalSumm(client.getTotalSumm()-contracts.getPrice());
            if(contracts.getEquipments()!=null){
                List<DevicesSqlDao> dev = contracts.getEquipments();
                for(int i = 0; i < dev.size(); i++){
                    DevicesSqlDao tem= dev.get(i);
                    tem.delOneCntr(contracts);
                    tem.setNwStr();
                    contracts.setPrice();
                }
                contracts.setEquipments(null);
            }
            contracts.setOldClient(null);

            clientsRepo.saveAll(Arrays.asList(client));
        }else if(contracts.getClient()== null && contracts.getOldClient()==null){
            if(contracts.getEquipments()!=null){
                List<DevicesSqlDao> dev = contracts.getEquipments();
                for(int i = 0; i < dev.size(); i++){
                    DevicesSqlDao tem= dev.get(i);
                    tem.delOneCntr(contracts);
                    tem.setNwStr();
                    contracts.setPrice();
                }
                contracts.setEquipments(null);
            }
        }
        contractsRepo.deleteById(id);
    }
    @Override
    public List<ContractsSqlDao> searchContracts(String fdate, String ldate, String name){
        log.info("Поиск контрактов за промежуток времени");
        List<ContractsSqlDao> allContracts = contractsRepo.findByCompNameContainingIgnoreCaseOrderByCompName(name);
        Long one = Long.parseLong(fdate);
        Long two = Long.parseLong(ldate);
        Date fDate = new Date(one);
        Date lDate = new Date(two);
        System.out.println(fDate);
        List<ContractsSqlDao> cont = allContracts.stream().filter(e->(( ((e.getDateLDate()).before(lDate))) )).collect(Collectors.toList());
        return cont;
    }
    @Override
    public Iterable<ContractsSqlDao> putDec(int id, ContractsSqlDao devices){
        log.info("Изменение контрактов");
        ContractsSqlDao contract = contractsRepo.findById(id).orElseThrow();
        if(contract.getOldClient()==null) {
            contract.setCompName(devices.getCompName());
            contract.setLDate(devices.getDateLDate());
            contract.setFDate(devices.getDateFDate());
            contract.setRelevance();
            int nowprice;
            if (contract.getClient() != null) {
                nowprice = contract.getClient().getTotalSumm() - contract.getPrice();
            } else {
                nowprice = 0;
            }
            if (devices.getTempStr() != null) {
                if (!devices.getTempStr().equals(contract.getTempStr())) {
                    if (!devices.getTempStr().equals("")) {
                        String[] array = devices.getTempStr().split(",");
                        List<Integer> intsList = new ArrayList<Integer>(array.length);
                        for (int i = 0; i < array.length; i++) {
                            intsList.add(i, Integer.parseInt(array[i]));
                        }
                        Iterable<DevicesSqlDao> cont = devicesRepo.findAllById(intsList);
                        List<DevicesSqlDao> target = new ArrayList<>();
                        cont.forEach(target::add);
                        for (DevicesSqlDao temp : target) {
                            contract.setOneEquip(temp);
                            temp.setOneContract(contract);
                            temp.setNwStr();
                            System.out.print(temp.getTempStr());
                        }
                        contract.setPrice();
                        if (devices.getTempStr() != null) {
                            contract.setTempStr(devices.getTempStr());
                        }
                        devicesRepo.saveAll(target);
                    }
                }
                if (contract.getClient() != null) {
                    contract.getClient().setTotalSumm(nowprice + contract.getPrice());
                }
            }
        }
        return contractsRepo.saveAll(Arrays.asList(contract));
    }
    @Override
    public Iterable<ContractsSqlDao> noContractId(int idc, int idd){
        log.info("Удаление девайса");
        ContractsSqlDao clie = contractsRepo.findById(idc).orElseThrow();
        DevicesSqlDao dev = devicesRepo.findById(idd).orElseThrow();
        dev.delOneCntr(clie);
        clie.delOneEquip(dev);
        clie.setTempStr(clie.getEquipments().toString());
        dev.setNwStr();
        clie.setPrice();
        if(clie.getClient() != null) {
            clie.getClient().setTotalSumm(clie.getPrice());
        }
        devicesRepo.saveAll(Arrays.asList(dev));
        return contractsRepo.saveAll(Arrays.asList(clie));
    }
    @Override
    public List<ContractsSqlDao> getDevices(){
        List <ContractsSqlDao> contractsList = contractsRepo.findAll();
        return contractsList;
    }
    @Override
    public void financePdf(List list) {
        log.info("Формирование отчета финансов pdf формат");
        Document document = new Document();
        try {
            BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 14, Font.NORMAL);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\asus\\Desktop\\Отчеты\\lab7\\Finance.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(7); // 7 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            table.setWidths(new int[]{15,50,20,35,35,15,90});
            PdfPCell cell0 = new PdfPCell(new Paragraph("№"));
            PdfPCell cell1 = new PdfPCell(new Paragraph("Name"));

            cell0.setPaddingLeft(10);
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell1.setPaddingLeft(10);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell2 = new PdfPCell(new Paragraph("Relevance"));

            cell2.setPaddingLeft(10);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell3 = new PdfPCell(new Paragraph("fDate"));

            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell4 = new PdfPCell(new Paragraph("lDate"));
            cell4.setPaddingLeft(10);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell5 = new PdfPCell(new Paragraph("id"));
            cell5.setPaddingLeft(10);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
            PdfPCell cell6 = new PdfPCell(new Paragraph("Equipments"));
            cell6.setPaddingLeft(10);
            cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell0);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            Iterable<ContractsSqlDao> dataset1 = contractsRepo.findAllById(list);
            List<ContractsSqlDao> dataset = new ArrayList<>();
            dataset1.forEach(dataset::add);

            //Set Column widths
            int i = 1;
            for (ContractsSqlDao record : dataset) {
                String nStr = "";
                table.addCell(String.valueOf(i));
                table.addCell(new Paragraph(record.getCompName(), font));
                table.addCell(String.valueOf(record.getRelevance()));
                table.addCell(String.valueOf(record.getFDate()));
                table.addCell(String.valueOf(record.getLDate()));
                table.addCell(String.valueOf(record.getId()));
                for (DevicesSqlDao devic : record.getEquipments()) {
                    nStr = nStr.concat("Наименование: ").concat(devic.getName()).concat("\n");
                    nStr =  nStr.concat("Количество: ").concat(Integer.toString(devic.getCountSale())).concat("\n").concat("Цена: ");
                    nStr = nStr.concat(Integer.toString(devic.getPrice()).concat("\n"));
                    nStr =  nStr.concat("Сумма: ").concat(Integer.toString(devic.getTotalSumm())).concat("\n");
                }
                nStr =nStr.concat("Итог:").concat(Integer.toString(record.getPrice())).concat("\n");
                table.addCell(new Paragraph(nStr, font));
                i++;
            }
            document.add(table);

            document.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void getPDF(List list)
    {
        log.info("pdf отчет по контрактам");
        Document document = new Document();
        try {
            BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 14, Font.NORMAL);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\asus\\Desktop\\Отчеты\\lab7\\AddTableContract.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(7); // 7 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            table.setWidths(new int[]{15,50,20,35,35,15,90});
            PdfPCell cell0 = new PdfPCell(new Paragraph("№"));
            PdfPCell cell1 = new PdfPCell(new Paragraph("Name"));

            cell0.setPaddingLeft(10);
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);

            cell1.setPaddingLeft(10);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell2 = new PdfPCell(new Paragraph("Relevance"));

            cell2.setPaddingLeft(10);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell3 = new PdfPCell(new Paragraph("fDate"));

            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell4 = new PdfPCell(new Paragraph("lDate"));
            cell4.setPaddingLeft(10);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell5 = new PdfPCell(new Paragraph("id"));
            cell5.setPaddingLeft(10);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
            PdfPCell cell6 = new PdfPCell(new Paragraph("Equipments"));
            cell6.setPaddingLeft(10);
            cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell0);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            Iterable<ContractsSqlDao> dataset1 = contractsRepo.findAllById(list);
            List<ContractsSqlDao> dataset = new ArrayList<>();
            dataset1.forEach(dataset::add);

            //Set Column widths
            int i = 1;
            for (ContractsSqlDao record : dataset) {
                String nStr = "";
                table.addCell(String.valueOf(i));
                table.addCell(new Paragraph(record.getCompName(), font));
                table.addCell(String.valueOf(record.getRelevance()));
                table.addCell(String.valueOf(record.getFDate()));
                table.addCell(String.valueOf(record.getLDate()));
                table.addCell(String.valueOf(record.getId()));
                for (DevicesSqlDao devic : record.getEquipments()) {
                    nStr = nStr.concat("Наименование: ").concat(devic.getName()).concat("\n");
                }
                table.addCell(new Paragraph(nStr, font));
                i++;
            }
            document.add(table);

            document.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ContractsSqlDao> getRelev(String name){
        log.info("Отбор релевантных контрактов");
        List <ContractsSqlDao> relList, contractsList;
        contractsList = contractsRepo.findByCompNameContainingIgnoreCaseOrderByCompName(name);
        relList = contractsList.stream().filter(element-> ((element.getRelevance()))).collect(Collectors.toList());
        return relList;
    }

    @Override
    public List<ContractsSqlDao> getNotRelev(String name){
        log.info("Список нерелевантных контрактов");
        List <ContractsSqlDao> relList, contractsList;
        contractsList = contractsRepo.findByCompNameContainingIgnoreCaseOrderByCompName(name);
        relList = contractsList.stream().filter(element-> ((!element.getRelevance()))).collect(Collectors.toList());
        return relList;
    }

}