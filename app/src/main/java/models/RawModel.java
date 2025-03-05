package models;

import java.util.ArrayList;
import java.util.List;

public class RawModel {

	private int vaoID;
	private int vertexCount;
	private List<Integer> vbos;

	public RawModel(int vaoID, int vertexCount) {
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
		this.vbos = new ArrayList<>();
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void addVbo(int vboID) {
		vbos.add(vboID);
	}

	public List<Integer> getVbos() {
		return vbos;
	}

}
