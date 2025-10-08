package org.ecom.panier;

import org.springframework.boot.SpringApplication;

public class TestPanierApplication {

    public static void main(String[] args) {
        SpringApplication.from(PanierApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
