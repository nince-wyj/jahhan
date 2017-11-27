package net.jahhan.utils;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ValidationUtil {
	private static Validator validator;

	static {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	public static void main(String[] args) {
		ValidationUtil u = new ValidationUtil();
		Car car = u.new Car(null, "DD-AB-123", 4);

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate(car);
		System.out.println(constraintViolations.iterator().next().getMessage());;
	}

	public class Car {

		@NotNull(message="")
		private String manufacturer;

		@NotNull
		@Size(min = 2, max = 14)
		private String licensePlate;

		@Min(2)
		private int seatCount;

		public Car(String manufacturer, String licencePlate, int seatCount) {
			this.manufacturer = manufacturer;
			this.licensePlate = licencePlate;
			this.seatCount = seatCount;
		}

		// getters and setters ...
	}
}
