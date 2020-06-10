package ir.malek.newsanalysis.ner;

public class Entity {
	private String name;
	private EntitySubType subtype;

	Entity(String name, EntitySubType subtype) {
		this.name = name;
		this.subtype = subtype;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if(!(obj instanceof Entity)){
			return false;
		}
		
		final Entity other = (Entity) obj;

		return this.name.equals(other.name) && this.subtype.equals(other.getSubType());
	}

	@Override
	public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + subtype.ordinal();
        return result;
	}

	public String getName() {
		return name;
	}

	public EntityType getType() {
		return subtype.getMainType();
	}

	public EntitySubType getSubType() {
		return subtype;
	}
}
