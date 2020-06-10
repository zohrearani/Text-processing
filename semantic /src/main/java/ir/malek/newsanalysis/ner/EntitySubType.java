package ir.malek.newsanalysis.ner;

public enum EntitySubType {
	
	IRANIAN_FIRST_NAME(EntityType.PER),
	IRANIAN_LAST_NAME(EntityType.PER),
	ARABIC_FIRST_NAME(EntityType.PER),
	ARABIC_LAST_NAME(EntityType.PER),
	US_FIRST_NAME(EntityType.PER),
	US_LAST_NAME(EntityType.PER),
	SURN(EntityType.PER),
	PER(EntityType.PER),
	
	IRANIAN_CITY(EntityType.LOC),
	FOREIGN_CITY(EntityType.LOC),
	IRANIAN_PROVINCE(EntityType.LOC),
	FOREIGN_PROVINCE(EntityType.LOC),
	COUNTRY(EntityType.LOC),
	IRANIAN_RIVER(EntityType.LOC),
	FOREIGN_RIVER(EntityType.LOC),
	LAKE(EntityType.LOC),
	SEA(EntityType.LOC),
	OCEAN(EntityType.LOC),
	GULF(EntityType.LOC),
	DAM(EntityType.LOC),
	ISLAND(EntityType.LOC),
	MOUNTAIN(EntityType.LOC),
	IRANIAN_FAMOUS_STREET(EntityType.LOC),
	TEHRAN_ZONE(EntityType.LOC),
	LOC(EntityType.LOC),
	
	IRANIAN_ORG(EntityType.ORG),
	FOREIGN_ORG(EntityType.ORG),
	ORG(EntityType.ORG);
	
	private final EntityType entityType;

	EntitySubType(EntityType entityType) {
		this.entityType = entityType;
	}

	public EntityType getMainType() {
		return entityType;
	}
}
