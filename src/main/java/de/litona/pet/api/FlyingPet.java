package de.litona.pet.api;

public interface FlyingPet extends WalkingPet {

	void setFlightAcceleration(float val);

	void setFallAcceleration(float val);

	float getFlightAcceleration();

	float getFallAcceleration();
}
