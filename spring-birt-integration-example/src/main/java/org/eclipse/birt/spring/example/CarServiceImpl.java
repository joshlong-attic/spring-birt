package org.eclipse.birt.spring.example;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
public class CarServiceImpl implements CarService {

    // todo make this data come from a data source
    public List<Car> getAllCars() {
        Car car1 = new Car();
        car1.setYear("2000");
        car1.setMake("Chevrolet");
        car1.setModel("Corvette");
        Car car2 = new Car();
        car2.setYear("2005");
        car2.setMake("Dodge");
        car2.setModel("Viper");
        Car car3 = new Car();
        car3.setYear("2002");
        car3.setMake("Ford");
        car3.setModel("Mustang GT");
        return Arrays.asList(car1, car2, car3);
    }
}
