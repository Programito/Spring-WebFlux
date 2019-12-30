package com.progra.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.progra.springboot.webflux.app.models.documents.Categoria;
import com.progra.springboot.webflux.app.models.documents.Producto;
import com.progra.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;



@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ProductoService service;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	// no se puede iniciar en application.properties con query
	@Override
	public void run(String... args) throws Exception {
		
		// eliminar la colleccion cada vez que se ejecuta
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria mouse = new Categoria("Mouse");
		Categoria  smartphone = new Categoria("Smartphone");
		Categoria impresora= new Categoria("Impresora");
		Categoria teclado = new Categoria("Teclado");
		
		// flatMap de thenMany para trabajar con observables y no mono como map
		//c-> service.saveCategoria(c)
		// thenMany para flux then para mono
		Flux.just(mouse,smartphone,impresora,teclado)
			.flatMap(service::saveCategoria)
			.doOnNext(c -> {
				log.info("Categoria creada " + c.getNombre() + ", Id: " + c.getId());
			}).thenMany(	Flux.just(new Producto("Tempest MS100 Paladin Ratón Gaming 1600DPI",3.99,mouse),
					new Producto("Razer DeathAdder Elite Ratón Gaming 16000 DPI Negro ",39.99,mouse),
					new Producto("Logitech MX Master 2S Ratón Inalámbrico Negro 1000DPI",69.99,mouse),
					new Producto("Logitech Wireless Combo MK270 Teclado Inalámbrico ",24.99,mouse),
					new Producto("Samsung Galaxy A40 4/64GB Coral Libre ",199.00,smartphone),
					new Producto("Samsung Galaxy A50 4/128GB Negro Libre ",275.95,smartphone),
					new Producto("Samsung Galaxy S10e Negro Libre ",579.00, smartphone),
					new Producto("Xiaomi Redmi Note 8 Pro 6/128Gb Verde Libre ",269.00,smartphone),
					new Producto("Xiaomi Redmi Note 8T 4/64GB Blanco Lunar Libre ",192.00,smartphone),
					new Producto("Brother HL-L2310D Impresora Láser Monocromo ",64.99,impresora),
					new Producto("Krom Kernel TKL Teclado Mecánico Gaming RGB Compacto ",49.99,teclado)	
					)
				.flatMap(producto-> {
						producto.setCreateAt(new Date());
						return service.save(producto);
					}))
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
		
	}

}
