import adapter.DatabaseStorage;
import domain.EntityInterface;
import domain.Product;
import service.ProductService;

import javax.xml.transform.Source;
import java.sql.SQLOutput;

void main() {
    ProductService productService = new ProductService();

    productService.create(new Product(productService.generateUUID(),
            "SKU" ,
            "nome",
            new BigDecimal(2)));

    productService.listAll();
//    Product product1 = new Product("TSH-BLK-002-SM", "Celular", new BigDecimal(1000));
//    Product product2 = new Product("APL-IP13PM-256-GRF", "Capa de Celular", new BigDecimal(100));
//    Product product3 = new Product("SAM-S22-128-PRE", "Película de Celular", new BigDecimal(120));
//
//    System.out.println("\nPRODUTO 1: ");
//    System.out.println(product1.toString());
//    product1.setPrice(new BigDecimal("999"));
//    System.out.println(product1.toString());
//
//    System.out.println("\nPRODUTO 2: ");
//    System.out.println(product2.toString());
//    product2.setPrice(new BigDecimal("80"));
//    System.out.println(product2.toString());
//
//    System.out.println("\nPRODUTO 3: ");
//    System.out.println(product3.toString());
//    product3.setPrice(new BigDecimal("60"));
//    System.out.println(product3.toString());
//
//
//    DatabaseStorage database = new DatabaseStorage();
//    //usem essa database virtual para fazer todas operacoes, adicionar, listar, remover, buscar apenas 1
//
//    database.save(product1);
//    ArrayList<EntityInterface>registros = database.listAll();
//
//    for (int i = 0; i < registros.size(); i++) {
//        System.out.println(registros.get(i));
//    }




    }