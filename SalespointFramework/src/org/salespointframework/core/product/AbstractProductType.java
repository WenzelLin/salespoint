package org.salespointframework.core.product;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.salespointframework.core.money.Money;
import org.salespointframework.core.product.features.ProductFeatureType;
import org.salespointframework.util.Objects;
import org.salespointframework.util.SalespointIterable;

@Entity
public class AbstractProductType implements ProductType {
	
	@Id
	protected String productIdentifier;
	protected Money price;
	
	//TODO Map?
	protected Set<ProductFeatureType> featureTypes = new HashSet<ProductFeatureType>() ;
	
	@Deprecated
	protected AbstractProductType() {
		
	}
	
	
	public AbstractProductType(String productIdentifier, Money price) {
		this.productIdentifier = Objects.requireNonNull(productIdentifier, "productIdentifier");
		this.price = Objects.requireNonNull(price, "price");
	}
	
	@Override
	public boolean equals(Object productType) {
		if(productType instanceof ProductType) {
			return this.equals((AbstractProductType)productType);
		} else {
			return false;
		}
	}
	
	public boolean equals(AbstractProductType other) {
		return this.productIdentifier.equals(other.productIdentifier);
	}
	
	@Override
	public int hashCode() {
		return productIdentifier.hashCode();
	}
	
	@Override	
	public Money getPrice() {
		return price;
	}

	@Override
	public Iterable<ProductFeatureType> getProductFeatureTypes() {
		return SalespointIterable.from(featureTypes);
	}
	
	@Override
	public String getProductIdentifier() {
		return productIdentifier;
	}
	
}