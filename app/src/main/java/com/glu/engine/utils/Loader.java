package com.glu.engine.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES30;
import android.util.JsonReader;
import android.util.Log;

import com.glu.engine.GUI.Bouton;
import com.glu.engine.GUI.ColorSquare;
import com.glu.engine.GUI.GUIBase;
import com.glu.engine.GUI.Glissoire;
import com.glu.engine.GUI.TexQuad;
import com.glu.engine.GUI.Text.Font;
import com.glu.engine.GUI.Text.TextBox;
import com.glu.engine.MainActivity;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Objects.GTexture;
import com.glu.engine.Objects.RawModel;
import com.glu.engine.Objects.SkyBox;
import com.glu.engine.Scene.Light;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.shader.Material;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Loader {

	private final MainActivity main;

	private static Loader loader;

	private Ressources ressources;

	private Loader(MainActivity main){
		this.main = main;
	}

	public static void init(MainActivity main){
		loader = new Loader(main);
		loader.ressources = Ressources.getRessources();
	}

	public static Loader getLoader(){
		return loader;
	}

	public static RawModel loadToVAO(float[] positions, float[] uv, float[] norm, int[] indices){
		long timer = System.currentTimeMillis();

		//tangent bitangent
		float[] tan = new float[positions.length];
		float[] bitan = new float[positions.length];

		if(uv.length > 0) {

			Vector3f P1,P2,P3,E1,E2,TAN,BITAN;
			Vector2f PUV1,PUV2,PUV3,dUV1,dUV2;
			float a;

			for (int i = 0; i < indices.length - 2; i += 3) {
				P1 = new Vector3f(positions[indices[i + 0] * 3 + 0], positions[indices[i + 0] * 3 + 1], positions[indices[i + 0] * 3 + 2]);
				P2 = new Vector3f(positions[indices[i + 1] * 3 + 0], positions[indices[i + 1] * 3 + 1], positions[indices[i + 1] * 3 + 2]);
				P3 = new Vector3f(positions[indices[i + 2] * 3 + 0], positions[indices[i + 2] * 3 + 1], positions[indices[i + 2] * 3 + 2]);

				E1 = Vector3f.sub(P2, P1);
				E2 = Vector3f.sub(P3, P1);

				PUV1 = new Vector2f(uv[indices[i + 0] * 2 + 0], uv[indices[i + 0] * 2 + 1]);
				PUV2 = new Vector2f(uv[indices[i + 1] * 2 + 0], uv[indices[i + 1] * 2 + 1]);
				PUV3 = new Vector2f(uv[indices[i + 2] * 2 + 0], uv[indices[i + 2] * 2 + 1]);

				dUV1 = Vector2f.sub(PUV2, PUV1);
				dUV2 = Vector2f.sub(PUV3, PUV1);

				a = 1.0f / ((dUV1.x * dUV2.y) - (dUV2.x * dUV1.y));

				TAN = Vector3f.sub(Vector3f.scale(E1, dUV2.y), Vector3f.scale(E2, dUV1.y));
				BITAN = Vector3f.sub(Vector3f.scale(E2, dUV1.x), Vector3f.scale(E1, dUV2.x));
				TAN.scale(a);
				TAN.normalize();
				BITAN.scale(a);
				BITAN.normalize();

				tan[indices[i + 0] * 3 + 0] = TAN.x;
				tan[indices[i + 0] * 3 + 1] = TAN.y;
				tan[indices[i + 0] * 3 + 2] = TAN.z;
				bitan[indices[i + 0] * 3 + 0] = BITAN.x;
				bitan[indices[i + 0] * 3 + 1] = BITAN.y;
				bitan[indices[i + 0] * 3 + 2] = BITAN.z;

				tan[indices[i + 1] * 3 + 0] = TAN.x;
				tan[indices[i + 1] * 3 + 1] = TAN.y;
				tan[indices[i + 1] * 3 + 2] = TAN.z;
				bitan[indices[i + 1] * 3 + 0] = BITAN.x;
				bitan[indices[i + 1] * 3 + 1] = BITAN.y;
				bitan[indices[i + 1] * 3 + 2] = BITAN.z;

				tan[indices[i + 2] * 3 + 0] = TAN.x;
				tan[indices[i + 2] * 3 + 1] = TAN.y;
				tan[indices[i + 2] * 3 + 2] = TAN.z;
				bitan[indices[i + 2] * 3 + 0] = BITAN.x;
				bitan[indices[i + 2] * 3 + 1] = BITAN.y;
				bitan[indices[i + 2] * 3 + 2] = BITAN.z;
			}
		}

		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInVAO(0,3, positions);
		storeDataInVAO(1,2, uv);
		storeDataInVAO(2,3,norm);
		storeDataInVAO(3,3,tan);
		storeDataInVAO(4,3,bitan);

		unbindVAO();

		Log.w("LoadToVAO", (System.currentTimeMillis() - timer) +" milliseconds to create VAO");

		return new RawModel(vaoID, indices.length, positions, uv, norm,indices);
	}

	public static RawModel loadToVAO(float[] positions){
		int vaoID = createVAO();

		storeDataInVAO(0,3,positions);
		unbindVAO();
		return new RawModel(vaoID,positions.length/3,positions,null,null,null);
	}

	public static RawModel loadTextToVAO(float[] positions, float[] UV){
		int vaoID = createVAO();
		storeDataInVAO(0,2,positions);
		storeDataInVAO(1,2,UV);
		unbindVAO();
		return new RawModel(vaoID,positions.length/2,UV,null,null,null);
	}

	public String loadAssetText(String directory) {
		long timer = System.currentTimeMillis();

		String result;
		try {
			AssetManager assets = main.getAssets();
			InputStream is = assets.open(directory);
			result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining(" \n"));
		}catch (Exception e){
			Log.e("File", "an error occurred: "+e.getMessage());
			e.printStackTrace();
			result = "";
		}

		Log.w("LoadAssetText", (System.currentTimeMillis() - timer) +" milliseconds to load " + directory);

		return result;
	}

	public RawModel loadAssetModel(String dir){
		long timerB = System.currentTimeMillis();

		RawModel model = null;

		InputStream is;
		try {
			AssetManager am = main.getAssets();
			is = am.open(dir);
		}catch (Exception e){
			Log.e("loadAssetModel",e.getMessage());
			is = null;
			System.exit(-1);
		}

		BufferedInputStream bis = new BufferedInputStream(is);

		boolean continueReading = true;

		List<Float> rawPos = new ArrayList<>();
		List<Float> rawNorm = new ArrayList<>();
		List<Float> rawUV = new ArrayList<>();
		List<Integer> rawIndices = new ArrayList<>();

		List<Float> indexPos = new ArrayList<>();
		List<Float> indexNorm = new ArrayList<>();
		List<Float> indexUV = new ArrayList<>();
		List<Integer> preIndex = new ArrayList<>();

		int minIndex = Integer.MAX_VALUE;
		int maxIndex = 0;

		int iAP = 0;
		int iAN = 0;
		int iAU = 0;

		int size;
		int charCount = 0;

		long timeProgressBar = System.currentTimeMillis();

		try {
			size = bis.available();
		} catch (IOException e) {
			e.printStackTrace();
			size = 1;
		}

		while(continueReading){

			boolean endOfLine = false;
			StringBuilder line = new StringBuilder();
			//load a line
			while(!endOfLine){
				char chr = 'a';
				try {
					chr = (char) bis.read(); // get a char
					charCount ++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(chr != '\uFFFF'){ // if it's not the end of the file
					line.append(chr);
					if(line.toString().endsWith("\n")){
						endOfLine = true;
						line = new StringBuilder(line.toString().replace("\n", ""));
					}
				}else{
					continueReading = false;
					endOfLine = true;
				}
			}

			String[] currentLine = line.toString().split(" "); // split between words
			//progressMarker
			if(System.currentTimeMillis() - timeProgressBar > 1000) {
				Log.w("loadModel", "loading... " + (100.0f * (float) charCount / (float) size) + "%");
				timeProgressBar = System.currentTimeMillis();
			}

			// if it's a vertex position
			if(line.toString().startsWith("v ")){
				rawPos.add(Float.parseFloat(currentLine[1])); // it starts at 1 because we have v|xxx|xxx|xxx
				rawPos.add(Float.parseFloat(currentLine[2]));
				rawPos.add(Float.parseFloat(currentLine[3]));
			}
			//if it's a vertex normal
			else if(line.toString().startsWith("vn ")){
				rawNorm.add(Float.parseFloat(currentLine[1]));
				rawNorm.add(Float.parseFloat(currentLine[2]));
				rawNorm.add(Float.parseFloat(currentLine[3]));
			}
			//if it's a uv coordinate
			else if(line.toString().startsWith("vt ")){
				rawUV.add(Float.parseFloat(currentLine[1]));
				rawUV.add(Float.parseFloat(currentLine[2]));
			}
			//if it's a face
			else if(line.toString().startsWith("f ")){
				//loop through the vertices, because we have f | (v1) pos/uv/norm | (v2) pos/uv/norm | (v3) pos/uv/norm
				for(int i = 1; i < currentLine.length; i++){
					String[] vertex = currentLine[i].split("/"); //split each vertex into it's components

					// there could be a case where one variable is missing i.e xx//xx,
					// and since parseFloat can't handle "" as a number, it crashes.
					int index;

					try{
						index = Math.max(Integer.parseInt(vertex[0]) - iAP,1) - 1;
						rawIndices.add(index);
						maxIndex = Math.max(index,maxIndex);
						minIndex = Math.min(index,minIndex);
					}catch(Exception e){
						rawIndices.add(0);
					}
					try{
						index = Math.max(Integer.parseInt(vertex[1]) - iAU,1) - 1;
						rawIndices.add(index);
					}catch(Exception e){
						rawIndices.add(0);
					}
					try{
						index = Math.max(Integer.parseInt(vertex[2]) - iAN,1) - 1;
						rawIndices.add(index);
					}catch(Exception e){
						rawIndices.add(0);
					}
				}
			}
			// if we hit the end of the file
			else if(!continueReading){
				// first of all, we have a list of vertex indices that is not optimized
				Log.w("loadModel","finished reading, now packing");

				int[] normIndex = new int[maxIndex - minIndex + 1];
				int[] uvIndex = new int[maxIndex - minIndex + 1];
				int[] ArrayListIndexTracker = new int[maxIndex - minIndex + 1];

				int tracker = 0;
				for (int i = 0; i < rawIndices.size(); i+= 3) {
					tracker ++;
					if(tracker > 1000) {
						Log.w("loadModel", "packing indices " + (100.0f * (float) i / (float) rawIndices.size()) + "%");
						tracker = 0;
					}
					if(normIndex[rawIndices.get(i) - minIndex] == 0 && uvIndex[rawIndices.get(i) - minIndex] == 0){
						// if the spot is unoccupied
						normIndex[rawIndices.get(i) - minIndex] = rawIndices.get(i + 2) + 1; // add one just so that we make sure
						uvIndex[rawIndices.get(i) - minIndex] = rawIndices.get(i + 1) + 1;   // that every non-used spot is 0

						// add it to the list
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 0));
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 1));
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 2));
						if(rawNorm.size() > 0) {
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 0));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 1));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 2));
						}else{
							indexNorm.add(0f);
							indexNorm.add(0f);
							indexNorm.add(0f);
						}
						if(rawUV.size() > 0) {
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 0));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 1));
						}else{
							indexUV.add(0f);
							indexUV.add(0f);
						}
						preIndex.add((indexPos.size() / 3) - 1); // add the index

						// keep track of the index, so we can reference it later, in case it is the same vertex
						ArrayListIndexTracker[rawIndices.get(i) - minIndex] = (indexPos.size() / 3 - 1);
					} else if(normIndex[rawIndices.get(i) - minIndex] - 1 == rawIndices.get(i + 2) && uvIndex[rawIndices.get(i) - minIndex] - 1 == rawIndices.get(i + 1)){
						// if the spot is occupied, but it is the same vertex, reference it
						preIndex.add(ArrayListIndexTracker[rawIndices.get(i) - minIndex]);
					}else{
						// if the spot is occupied by a different vertex, put it somewhere else
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 0));
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 1));
						indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 2));
						if(rawNorm.size() > 0) {
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 0));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 1));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 2));
						}else{
							indexNorm.add(0f);
							indexNorm.add(0f);
							indexNorm.add(0f);
						}
						if(rawUV.size() > 0) {
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 0));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 1));
						}else{
							indexUV.add(0f);
							indexUV.add(0f);
						}
						preIndex.add((indexPos.size() / 3) - 1);
					}
				}

				if(preIndex.size() < Integer.MAX_VALUE){

					// now that we have all the info we need, make a model
					float[] pos = new float[indexPos.size()];
					float[] norm = new float[indexNorm.size()];
					float[] uv = new float[indexUV.size()];
					int[] index = new int[preIndex.size()];
					//convert form List to Array
					for (int i = 0; i < indexPos.size(); i++) {
						pos[i] = indexPos.get(i);
					}
					for(int i = 0; i < indexNorm.size(); i++){
						norm[i] = indexNorm.get(i);
					}
					for (int i = 0; i < uv.length; i++) {
						uv[i] = indexUV.get(i);
					}
					for (int i = 0; i < index.length; i++) {
						index[i] = preIndex.get(i);
					}

					//Log.w("loading", " : " + (rawPos.size() + rawNorm.size() + rawUV.size()));

					model = new RawModel(pos, uv, norm, index);
					model.name = dir;
				}else{
					Log.e("index error", "GluEngine only supports up to " + Integer.MAX_VALUE);
				}
				indexPos.clear();
				indexNorm.clear();
				indexUV.clear();
				preIndex.clear();
				rawIndices.clear();
			}

		}

		Log.w("load model", (System.currentTimeMillis() - timerB) + "ms to load " + dir);

		return model;
	}

	public RawModel[] loadMultipleAssetModels(String dir){
		long timerA = System.currentTimeMillis();
		long timerB = System.currentTimeMillis();

		ArrayList<RawModel> rawModels = new ArrayList<>();

		InputStream is;
		try {
			AssetManager am = main.getAssets();
			is = am.open(dir);
		}catch (Exception e){
			Log.e("loadMultipleAssetModels",e.getMessage());
			is = null;
			System.exit(-1);
		}

		BufferedInputStream bis = new BufferedInputStream(is);

		boolean continueReading = true;
		boolean isFirstObject = true;

		List<Float> rawPos = new ArrayList<>();
		List<Float> rawNorm = new ArrayList<>();
		List<Float> rawUV = new ArrayList<>();
		List<Integer> rawIndices = new ArrayList<>();

		List<Float> indexPos = new ArrayList<>();
		List<Float> indexNorm = new ArrayList<>();
		List<Float> indexUV = new ArrayList<>();
		List<Integer> preIndex = new ArrayList<>();

		List<String> tooBigNames = new ArrayList<>();
		String name = "";

		int minIndex = Integer.MAX_VALUE;
		int maxIndex = 0;

		int iAP = 0;
		int iAN = 0;
		int iAU = 0;

		int charCount = 0;
		int size = 0;
		try {
			size = bis.available();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		long timeProgressBar = System.currentTimeMillis();

		while(continueReading){

			boolean endOfLine = false;
			StringBuilder line = new StringBuilder();
			//load a line
			while(!endOfLine){
				char chr = 'a';
				try {
					chr = (char) bis.read(); // get a char
					charCount++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(chr != '\uFFFF'){ // if it's not the end of the file
					line.append(chr);
					if(line.toString().endsWith("\n")){
						endOfLine = true;
						line = new StringBuilder(line.toString().replace("\n", ""));
					}
					if(line.toString().endsWith("\r")){
						endOfLine = true;
						line = new StringBuilder(line.toString().replace("\r", ""));
					}
				}else{
					continueReading = false;
					endOfLine = true;
				}
			}

			String[] currentLine = line.toString().split(" "); // split between words
			//progressMarker
			if(System.currentTimeMillis() - timeProgressBar > 1000) {
				Log.w("loadModels", "loading... " + (100.0f * (float) charCount / (float) size) + "%");
				timeProgressBar = System.currentTimeMillis();
			}

			// if it's a vertex position
			if(line.toString().startsWith("v ")){
				rawPos.add(Float.parseFloat(currentLine[1])); // it starts at 1 because we have v|xxx|xxx|xxx
				rawPos.add(Float.parseFloat(currentLine[2]));
				rawPos.add(Float.parseFloat(currentLine[3]));
			}
			//if it's a vertex normal
			else if(line.toString().startsWith("vn ")){
				rawNorm.add(Float.parseFloat(currentLine[1]));
				rawNorm.add(Float.parseFloat(currentLine[2]));
				rawNorm.add(Float.parseFloat(currentLine[3]));
			}
			//if it's a uv coordinate
			else if(line.toString().startsWith("vt ")){
				rawUV.add(Float.parseFloat(currentLine[1]));
				rawUV.add(Float.parseFloat(currentLine[2]));
			}
			//if it's a face
			else if(line.toString().startsWith("f ")){
				//loop through the vertices, because we have f | (v1) pos/uv/norm | (v2) pos/uv/norm | (v3) pos/uv/norm
				for(int i = 1; i < currentLine.length; i++){
					String[] vertex = currentLine[i].split("/"); //split each vertex into it's components

					// there could be a case where one variable is missing, for example: xx//xx,
					// and since parseFloat can't handle "" as a number, it crashes.
					int index;

					try{
						index = Math.max(Integer.parseInt(vertex[0]) - iAP,1) - 1;
						rawIndices.add(index);
						maxIndex = Math.max(index,maxIndex);
						minIndex = Math.min(index,minIndex);
					}catch(Exception e){
						rawIndices.add(0);
					}
					try{
						index = Math.max(Integer.parseInt(vertex[1]) - iAU,1) - 1;
						rawIndices.add(index);
					}catch(Exception e){
						rawIndices.add(0);
					}
					try{
						index = Math.max(Integer.parseInt(vertex[2]) - iAN,1) - 1;
						rawIndices.add(index);
					}catch(Exception e){
						rawIndices.add(0);
					}
				}
			}
			// if it's a new object or we hit the end of the file
			else if(line.toString().startsWith("o ") || !continueReading){
				// unless this is the first object, this means that we hit the end of the previous object's data
				if(!isFirstObject){
					// first of all, we have a list of vertex indices that is not optimized

					int[] normIndex = new int[maxIndex - minIndex + 1];
					int[] uvIndex = new int[maxIndex - minIndex + 1];
					int[] ArrayListIndexTracker = new int[maxIndex - minIndex + 1];

					for (int i = 0; i < rawIndices.size(); i+= 3) {
						if(normIndex[rawIndices.get(i) - minIndex] == 0 && uvIndex[rawIndices.get(i) - minIndex] == 0){
							// if the spot is unoccupied
							normIndex[rawIndices.get(i) - minIndex] = rawIndices.get(i + 2) + 1; // add one just so that we make sure
							uvIndex[rawIndices.get(i) - minIndex] = rawIndices.get(i + 1) + 1;   // that every non-used spot is 0

							// add it to the list
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 0));
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 1));
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 2));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 0));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 1));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 2));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 0));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 1));
							preIndex.add((indexPos.size() / 3) - 1); // add the index

							// keep track of the index, so we can reference it later, in case it is the same vertex
							ArrayListIndexTracker[rawIndices.get(i) - minIndex] = (indexPos.size() / 3 - 1);
						} else if(normIndex[rawIndices.get(i) - minIndex] - 1 == rawIndices.get(i + 2) && uvIndex[rawIndices.get(i) - minIndex] - 1 == rawIndices.get(i + 1)){
							// if the spot is occupied, but it is the same vertex, reference it
							preIndex.add(ArrayListIndexTracker[rawIndices.get(i) - minIndex]);
						}else{
							// if the spot is occupied by a different vertex, put it somewhere else
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 0));
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 1));
							indexPos.add(rawPos.get(rawIndices.get(i + 0) * 3 + 2));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 0));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 1));
							indexNorm.add(rawNorm.get(rawIndices.get(i + 2) * 3 + 2));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 0));
							indexUV.add(rawUV.get(rawIndices.get(i + 1) * 2 + 1));
							preIndex.add((indexPos.size() / 3) - 1);
						}
					}

					if(preIndex.size() < Integer.MAX_VALUE){

						// now that we have all the info we need, make a model
						float[] pos = new float[indexPos.size()];
						float[] norm = new float[indexNorm.size()];
						float[] uv = new float[indexUV.size()];
						int[] index = new int[preIndex.size()];
						//convert form List to Array
						for (int i = 0; i < indexPos.size(); i++) {
							pos[i] = indexPos.get(i);
							norm[i] = indexNorm.get(i);
						}
						for (int i = 0; i < uv.length; i++) {
							uv[i] = indexUV.get(i);
						}
						for (int i = 0; i < index.length; i++) {
							index[i] = preIndex.get(i);
						}

						//Log.w("loading", "sizes : " + (rawPos.size() + rawNorm.size() + rawUV.size()));

						RawModel model = new RawModel(pos, uv, norm, index);
						model.name = name;

						rawModels.add(model);
						Log.w("load model", (System.currentTimeMillis() - timerA) + "ms to load " + name);
						timerA = System.currentTimeMillis();
					}else{
						//keep track of the models that are too big.
						Log.e("index error", "GluEngine only supports up to " + Integer.MAX_VALUE);
						tooBigNames.add(name);
					}
					indexPos.clear();
					indexNorm.clear();
					indexUV.clear();
					preIndex.clear();
					rawIndices.clear();

				}else{
					isFirstObject = false;
				}

				name = line.toString().replace("o ", "");
			}

		}

		Log.w("load model", tooBigNames.size() + " models were too big. Here they are : ");
		for (int i = 0; i < tooBigNames.size(); i++) {
			Log.w("too big models",tooBigNames.get(i));
		}

		Log.w("load model", (System.currentTimeMillis() - timerB) + "ms to load " + dir);

		//convert from ArrayList to array
		RawModel[] models = new RawModel[rawModels.size()];
		for (int i = 0; i < rawModels.size(); i ++) {
			models[i] = rawModels.get(i);
		}

		return models;
	}

	@SuppressWarnings("ConstantConditions")
	public Scene loadScene(String directory, Vector2f resolution) {
		long timer = System.currentTimeMillis();
		Scene scene = new Scene();

		try {
			// load Ressources
			boolean hasLoadedRessources = false;
			if (!hasLoadedRessources) {
				JsonReader jsonRessourceReader = null;
				try {
					AssetManager assets = main.getAssets();
					InputStream is = assets.open("Scenes/RessourcesRef.json");
					jsonRessourceReader = new JsonReader(new InputStreamReader(is));
				} catch (Exception e) {
					Log.e("loadScene", "an error occurred: " + e.getMessage());
					e.printStackTrace();
				}

				jsonRessourceReader.beginObject();
				while (jsonRessourceReader.hasNext()) {
					String name = jsonRessourceReader.nextName();
					switch (name) {
						case "Meshes":
							jsonRessourceReader.beginArray();
							while (jsonRessourceReader.hasNext()) {
								jsonRessourceReader.beginObject();

								String meshName = null;
								String meshDir = null;
								while (jsonRessourceReader.hasNext()) {
									String tag = jsonRessourceReader.nextName();
									switch (tag) {
										case "name":
											meshName = jsonRessourceReader.nextString();
											break;
										case "directory":
											meshDir = jsonRessourceReader.nextString();
											break;
										default:
											jsonRessourceReader.skipValue();
											break;
									}
								}
								ressources.addMesh(meshName, meshDir);

								jsonRessourceReader.endObject();
							}
							jsonRessourceReader.endArray();
							break;
						case "Textures":
							jsonRessourceReader.beginArray();
							while (jsonRessourceReader.hasNext()) {
								jsonRessourceReader.beginObject();

								String textName = null;
								String textDir = null;
								while (jsonRessourceReader.hasNext()) {
									String tag = jsonRessourceReader.nextName();
									switch (tag) {
										case "name":
											textName = jsonRessourceReader.nextString();
											break;
										case "directory":
											textDir = jsonRessourceReader.nextString();
											break;
										default:
											jsonRessourceReader.skipValue();
											break;
									}
								}
								ressources.addTexture(textName, textDir);

								jsonRessourceReader.endObject();
							}
							jsonRessourceReader.endArray();
							break;
						case "Fonts":
							jsonRessourceReader.beginArray();
							while (jsonRessourceReader.hasNext()) {
								jsonRessourceReader.beginObject();

								String fontName = null;
								String fontDir = null;
								int size = 0;
								int padding = 0;
								ArrayList<Integer> ranges = new ArrayList<>();
								while (jsonRessourceReader.hasNext()) {
									String tag = jsonRessourceReader.nextName();
									switch (tag) {
										case "name":
											fontName = jsonRessourceReader.nextString();
											break;
										case "directory":
											fontDir = jsonRessourceReader.nextString();
											break;
										case "size":
											size = jsonRessourceReader.nextInt();
											break;
										case "padding":
											padding = jsonRessourceReader.nextInt();
											break;
										case "ranges":
											jsonRessourceReader.beginArray();
											while (jsonRessourceReader.hasNext()) {
												ranges.add(jsonRessourceReader.nextInt());
											}
											jsonRessourceReader.endArray();
											break;
										default:
											jsonRessourceReader.skipValue();
											break;
									}
								}
								int[] rangesInt = new int[ranges.size()];
								for (int i = 0; i < rangesInt.length; i++) {
									rangesInt[i] = ranges.get(i);
								}
								ressources.addFont(fontName, fontDir, size, padding, rangesInt);

								jsonRessourceReader.endObject();
							}
							jsonRessourceReader.endArray();
							break;
						default:
							jsonRessourceReader.skipValue();
							break;
					}

				}
				jsonRessourceReader.endObject();
			}

			JsonReader jsonSceneReader = null;
			try {
				AssetManager assets = main.getAssets();
				InputStream is = assets.open(directory);
				jsonSceneReader = new JsonReader(new InputStreamReader(is));
			} catch (Exception e) {
				Log.e("loadScene", "an error occurred: " + e.getMessage());
				e.printStackTrace();
			}

			ArrayList<Material> materials = new ArrayList<>();
			ArrayList<String> materialNames = new ArrayList<>();
			ArrayList<String> textureUsed = new ArrayList<>();
			ArrayList<String> fontUsed = new ArrayList<>();
			ArrayList<GUIBase> guiBaseElements = new ArrayList<>();
			ArrayList<String> guiBaseElementsNames = new ArrayList<>();

			jsonSceneReader.beginObject();
			while (jsonSceneReader.hasNext()) {
				String type = jsonSceneReader.nextName();
				switch (type) {
					case "materials":
						// only there to collapse in code editor
						if (true) {
							jsonSceneReader.beginArray();
							while (jsonSceneReader.hasNext()) {
								String name = null;
								int MatType = 0;
								boolean isColorTextured = false;
								String textureName = null;
								Vector3f color = null;
								float eIntensity = 0f;
								float roughness = 0f;
								boolean isNormalMapped = false;
								String normalMapName = null;
								boolean alphaClipped = false;
								boolean isRoughnessMapped = false;

								jsonSceneReader.beginObject();
								while (jsonSceneReader.hasNext()) {
									String tag = jsonSceneReader.nextName();
									switch (tag) {
										case "name":
											name = jsonSceneReader.nextString();
											break;
										case "type":
											MatType = jsonSceneReader.nextInt();
											break;
										case "isColorTextured":
											isColorTextured = jsonSceneReader.nextBoolean();
											break;
										case "texture":
											textureName = jsonSceneReader.nextString();
											break;
										case "color":
											float[] col = new float[3];
											jsonSceneReader.beginArray();
											for (int i = 0; i < 3; i++) {
												col[i] = (float) jsonSceneReader.nextDouble();
											}
											jsonSceneReader.endArray();
											color = new Vector3f(col[0], col[1], col[2]);
											break;
										case "emissionIntensity":
											eIntensity = (float) jsonSceneReader.nextDouble();
											break;
										case "roughness":
											roughness = (float) jsonSceneReader.nextDouble();
											break;
										case "isNormalMapped":
											isNormalMapped = jsonSceneReader.nextBoolean();
											break;
										case "normalMap":
											normalMapName = jsonSceneReader.nextString();
											break;
										case "isRoughnessMapped":
											isRoughnessMapped = jsonSceneReader.nextBoolean();
											break;
										case "alphaClip":
											alphaClipped = jsonSceneReader.nextBoolean();
											break;
										default:
											jsonSceneReader.skipValue();
											break;
									}
								}
								jsonSceneReader.endObject();

								Material mat = new Material(name, MatType, isColorTextured, textureName, color, alphaClipped, eIntensity, isRoughnessMapped, roughness, isNormalMapped, normalMapName);
								if (isColorTextured) {
									textureUsed.add(textureName);
									mat.setTexture(ressources.getTexture(textureName));
								}
								if (isNormalMapped) {
									textureUsed.add(normalMapName);
									mat.setNormal(ressources.getTexture(normalMapName));
								}

								materials.add(mat);
								materialNames.add(name);
							}
							jsonSceneReader.endArray();
						}
						break;
					case "skybox":
						// only there to collapse in code editor
						if (true) {
							jsonSceneReader.beginObject();

							String textureName = null;
							RawModel model = null;
							float intensity = 0f;
							while (jsonSceneReader.hasNext()) {
								String tag = jsonSceneReader.nextName();
								switch (tag) {
									case "HDRI":
										textureName = jsonSceneReader.nextString();
										break;
									case "Mesh":
										String meshName = jsonSceneReader.nextString();
										model = ressources.getMesh(meshName);
										break;
									case "intensity":
										intensity = (float) jsonSceneReader.nextDouble();
										break;
									default:
										jsonSceneReader.skipValue();
										break;
								}
							}

							GTexture texture = ressources.getTexture(textureName);
							textureUsed.add(textureName);
							SkyBox skyBox = new SkyBox(model, texture);
							skyBox.strength = intensity;
							scene.setSkybox(skyBox);

							jsonSceneReader.endObject();
						}
						break;
					case "Entities":
						if (true) {
							jsonSceneReader.beginArray();

							while (jsonSceneReader.hasNext()) {

								String name = null;
								String meshName = null;
								boolean separateMesh = false;
								Vector3f position = null;
								Vector3f scale = null;
								Vector3f rotation = null;
								String matName = null;

								jsonSceneReader.beginObject();
								while (jsonSceneReader.hasNext()) {
									String tag = jsonSceneReader.nextName();
									switch (tag) {
										case "name":
											name = jsonSceneReader.nextString();
											break;
										case "mesh":
											meshName = jsonSceneReader.nextString();
											break;
										case "position":
											jsonSceneReader.beginArray();
											float[] pos = new float[3];
											for (int i = 0; i < 3; i++) {
												pos[i] = (float) jsonSceneReader.nextDouble();
											}
											position = new Vector3f(pos[0], pos[1], pos[2]);
											jsonSceneReader.endArray();
											break;
										case "rotation":
											jsonSceneReader.beginArray();
											float[] rot = new float[3];
											for (int i = 0; i < 3; i++) {
												rot[i] = (float) jsonSceneReader.nextDouble();
											}
											rotation = new Vector3f(rot[0], rot[1], rot[2]);
											jsonSceneReader.endArray();
											break;
										case "scale":
											jsonSceneReader.beginArray();
											float[] s = new float[3];
											for (int i = 0; i < 3; i++) {
												s[i] = (float) jsonSceneReader.nextDouble();
											}
											scale = new Vector3f(s[0], s[1], s[2]);
											jsonSceneReader.endArray();
											break;
										case "material":
											matName = jsonSceneReader.nextString();
											break;
										case "SeparateMesh":
											separateMesh = jsonSceneReader.nextBoolean();
											break;
										default:
											jsonSceneReader.skipValue();
											break;
									}
								}
								if (!separateMesh) {
									Entity e = new Entity(ressources.getMesh(meshName));
									e.setPosition(position, 0);
									e.setRotation(rotation, 0);
									e.setScale(scale, 0);
									e.setMaterial(0, materials.get(materialNames.indexOf(matName)));
									e.name = name;
									scene.addEntity(e);
								} else {
									RawModel[] meshes = loadMultipleAssetModels(ressources.getMeshDir(meshName));
									for (int i = 0; i < meshes.length; i++) {
										Entity e = new Entity(meshes[i]);
										e.setPosition(position, 0);
										e.setRotation(rotation, 0);
										e.setScale(scale, 0);
										e.setMaterial(0, materials.get(materialNames.indexOf(matName)));
										e.name = name + i;
										scene.addEntity(e);
									}
								}
								jsonSceneReader.endObject();

							}
							jsonSceneReader.endArray();
						}
						break;
					case "lights":
						if (true) {
							jsonSceneReader.beginArray();
							while (jsonSceneReader.hasNext()) {
								Vector3f pos = null;
								Vector3f col = null;
								float intensity = 0f;

								jsonSceneReader.beginObject();
								while (jsonSceneReader.hasNext()) {
									String tag = jsonSceneReader.nextName();
									switch (tag) {
										case "position":
											jsonSceneReader.beginArray();
											float[] p = new float[3];
											for (int i = 0; i < 3; i++) {
												p[i] = (float) jsonSceneReader.nextDouble();
											}
											jsonSceneReader.endArray();

											pos = new Vector3f(p[0], p[1], p[2]);
											break;
										case "color":
											jsonSceneReader.beginArray();
											float[] c = new float[3];
											for (int i = 0; i < 3; i++) {
												c[i] = (float) jsonSceneReader.nextDouble();
											}
											jsonSceneReader.endArray();

											col = new Vector3f(c[0], c[1], c[2]);
											break;
										case "intensity":
											intensity = (float) jsonSceneReader.nextDouble();
											break;
										default:
											jsonSceneReader.skipValue();
											break;
									}
								}
								jsonSceneReader.endObject();

								scene.addLight(new Light(pos, col, intensity));
							}
							jsonSceneReader.endArray();
						}
						break;
					case "UI":
						if (true) {
							jsonSceneReader.beginObject();
							while (jsonSceneReader.hasNext()) {
								String tag = jsonSceneReader.nextName();
								switch (tag) {
									case "TexQuads":
										jsonSceneReader.beginArray();
										while (jsonSceneReader.hasNext()) {
											String name = null;
											String textureName = null;
											Vector2f position = null;
											Vector2f scale = null;
											float rotation = 0f;

											jsonSceneReader.beginObject();
											while (jsonSceneReader.hasNext()) {
												String quadTag = jsonSceneReader.nextName();
												switch (quadTag) {
													case "name":
														name = jsonSceneReader.nextString();
														break;
													case "texture":
														textureName = jsonSceneReader.nextString();
														break;
													case "position":
														float[] pos = new float[2];
														jsonSceneReader.beginArray();
														for (int i = 0; i < 2; i++) {
															pos[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														position = new Vector2f(pos[0], pos[1]);
														break;
													case "rotation":
														rotation = (float) jsonSceneReader.nextDouble();
														break;
													case "scale":
														float[] sc = new float[2];
														jsonSceneReader.beginArray();
														for (int i = 0; i < 2; i++) {
															sc[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														scale = new Vector2f(sc[0], sc[1]);
														break;
													default:
														jsonSceneReader.skipValue();
														break;
												}
											}
											jsonSceneReader.endObject();

											textureUsed.add(textureName);
											TexQuad tq = new TexQuad(ressources.getTexture(textureName));
											tq.name = name;
											tq.position.set(0, position);
											tq.rotation.set(0, rotation);
											tq.scale.set(0, scale);
											scene.addTexQuad(tq);

											guiBaseElements.add(tq);
											guiBaseElementsNames.add(name);
										}
										jsonSceneReader.endArray();
										break;
									case "ColorSquare":
										jsonSceneReader.beginArray();
										while (jsonSceneReader.hasNext()) {
											String name = null;
											Vector4f color = null;
											Vector2f position = null;
											Vector2f scale = null;
											float rotation = 0f;
											float rayonCoin = 0f;

											jsonSceneReader.beginObject();
											while (jsonSceneReader.hasNext()) {
												String quadTag = jsonSceneReader.nextName();
												switch (quadTag) {
													case "name":
														name = jsonSceneReader.nextString();
														break;
													case "color":
														jsonSceneReader.beginArray();
														float[] c = new float[4];
														for (int i = 0; i < 4; i++) {
															c[i] = (float) jsonSceneReader.nextDouble();
														}
														color = new Vector4f(c[0], c[1], c[2], c[3]);
														jsonSceneReader.endArray();
														break;
													case "position":
														float[] pos = new float[2];
														jsonSceneReader.beginArray();
														for (int i = 0; i < 2; i++) {
															pos[i] = (float) jsonSceneReader.nextDouble();
														}
														position = new Vector2f(pos[0], pos[1]);
														jsonSceneReader.endArray();

														break;
													case "rotation":
														rotation = (float) jsonSceneReader.nextDouble();
														break;
													case "scale":
														float[] sc = new float[2];
														jsonSceneReader.beginArray();
														for (int i = 0; i < 2; i++) {
															sc[i] = (float) jsonSceneReader.nextDouble();
														}
														scale = new Vector2f(sc[0], sc[1]);
														jsonSceneReader.endArray();
														break;
													case "rayonCoin" :
														rayonCoin = (float) jsonSceneReader.nextDouble();
														break;
													default:
														jsonSceneReader.skipValue();
														break;
												}
											}
											jsonSceneReader.endObject();

											ColorSquare cs = new ColorSquare( color);
											cs.name = name;
											cs.scale.set(0, scale);
											cs.position.set(0, position);
											cs.rotation.set(0, rotation);
											cs.cornerRadius = rayonCoin;
											scene.addColorSquare(cs);

											guiBaseElements.add(cs);
											guiBaseElementsNames.add(name);
										}
										jsonSceneReader.endArray();
										break;
									case "TextBoxes":
										jsonSceneReader.beginArray();
										while (jsonSceneReader.hasNext()){
											jsonSceneReader.beginObject();

											String name = null;
											String fontName = null;
											String text = null;
											Vector2f size = null;
											Vector2f position = null;
											float rotation = 0f;
											Vector2f scale = null;

											while (jsonSceneReader.hasNext()){
												String textTag = jsonSceneReader.nextName();
												switch (textTag){
													case "name":
														name = jsonSceneReader.nextString();
														break;
													case "font":
														fontName = jsonSceneReader.nextString();
														break;
													case "text":
														text = jsonSceneReader.nextString();
														break;
													case "size" :
														jsonSceneReader.beginArray();
														float[] s = new float[2];
														for (int i = 0; i < 2; i++) {
															s[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														size = new Vector2f(s[0],s[1]);
														break;
													case "position" :
														jsonSceneReader.beginArray();
														float[] p = new float[2];
														for (int i = 0; i < 2; i++) {
															p[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														position = new Vector2f(p[0],p[1]);
														break;
													case "rotation" :
														rotation = (float) jsonSceneReader.nextDouble();
														break;
													case "scale" :
														jsonSceneReader.beginArray();
														float[] sc = new float[2];
														for (int i = 0; i < 2; i++) {
															sc[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														scale = new Vector2f(sc[0],sc[1]);
														break;
												}
											}
											jsonSceneReader.endObject();

											fontUsed.add(fontName);
											TextBox textBox = new TextBox(ressources.getFont(fontName),size,position,scale,rotation);
											textBox.setText(text,2,4, TextBox.Alignment.LEFT);
											textBox.name = name;
											scene.addTextBox(textBox);
										}
										jsonSceneReader.endArray();
										break;
									case "boutons" :
										jsonSceneReader.beginArray();
										while (jsonSceneReader.hasNext()){
											jsonSceneReader.beginObject();

											String name = null;
											GUIBase bDéfaut = null;
											GUIBase bPressé = null;
											GUIBase bSurvol = null;
											Vector2f taille = null;
											Vector2f position = null;
											float rotation = 0f;
											Bouton.Préréglages préréglage = null;

											Bouton.EnSurvol enSurvol = null;
											Bouton.EnSurvolDébarque enSurvolDébarque = null;
											Bouton.EnSurvolRelâche enSurvolRelâche = null;
											Bouton.EnDoigtTouché enDoigtTouché = null;
											Bouton.EnDoigtDébarque enDoigtDébarque = null;
											Bouton.EnDoigtRelâche enDoigtRelâche = null;
											Bouton.EnClic enClic = null;

											while (jsonSceneReader.hasNext()){
												String textTag = jsonSceneReader.nextName();
												switch (textTag){
													case "nom":
														name = jsonSceneReader.nextString();
														break;
													case "défaut":
														String bDéfautNom = jsonSceneReader.nextString();
														int index = guiBaseElementsNames.indexOf(bDéfautNom);
														bDéfaut = guiBaseElements.get(index);
														break;
													case "pressé":
														String bPresséNom = jsonSceneReader.nextString();
														index = guiBaseElementsNames.indexOf(bPresséNom);
														bPressé = guiBaseElements.get(index);
														break;
													case "survol":
														String bSurvolNom = jsonSceneReader.nextString();
														if(!bSurvolNom.equals("null")) {
															index = guiBaseElementsNames.indexOf(bSurvolNom);
															bSurvol = guiBaseElements.get(index);
														}
														break;
													case "taille" :
														jsonSceneReader.beginArray();
														float[] s = new float[2];
														for (int i = 0; i < 2; i++) {
															s[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														taille = new Vector2f(s[0],s[1]);
														break;
													case "position" :
														jsonSceneReader.beginArray();
														float[] p = new float[2];
														for (int i = 0; i < 2; i++) {
															p[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														position = new Vector2f(p[0],p[1]);
														break;
													case "rotation" :
														rotation = (float) jsonSceneReader.nextDouble();
														break;
													case "préréglage" :
														String prérég = jsonSceneReader.nextString();
														switch(prérég){
															case "BOUTON":
																préréglage = Bouton.Préréglages.BOUTON;
																break;
															case "SÉLECTION":
																préréglage = Bouton.Préréglages.SÉLECTION;
																break;
															case "CONTRÔLES":
																préréglage = Bouton.Préréglages.CONTRÔLES;
																break;
															case "CASE_À_COCHER":
																préréglage = Bouton.Préréglages.CASE_À_COCHER;
																break;
														}
														break;
													case "comportement" :
														jsonSceneReader.beginObject();
														while (jsonSceneReader.hasNext()){
															String comp = jsonSceneReader.nextName();
															String option;
															switch (comp){
																case "EnDoigtTouché" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "rien" :
																			enDoigtTouché = Bouton.EnDoigtTouché.RIEN;
																			break;
																		case "survol" :
																			enDoigtTouché = Bouton.EnDoigtTouché.SURVOL;
																			break;
																		case "clic" :
																			enDoigtTouché = Bouton.EnDoigtTouché.CLIC;
																			break;
																	}
																	break;
																case "EnDoigtRelâche" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "reste" :
																			enDoigtRelâche = Bouton.EnDoigtRelâche.RESTE;
																			break;
																		case "clic" :
																			enDoigtRelâche = Bouton.EnDoigtRelâche.CLIC;
																			break;
																		case "retourne défaut" :
																			enDoigtRelâche = Bouton.EnDoigtRelâche.RETOURNE_DÉFAUT;
																			break;
																		case "survol" :
																			enDoigtRelâche = Bouton.EnDoigtRelâche.SURVOL;
																			break;
																	}
																	break;
																case "EnDoigtDébarque" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "reste" :
																			enDoigtDébarque = Bouton.EnDoigtDébarque.RESTE;
																			break;
																		case "clic" :
																			enDoigtDébarque = Bouton.EnDoigtDébarque.CLIC;
																			break;
																		case "retourne défaut" :
																			enDoigtDébarque = Bouton.EnDoigtDébarque.RETOURNE_DÉFAUT;
																			break;
																	}
																	break;
																case "EnSurvol" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "rien" :
																			enSurvol = Bouton.EnSurvol.RIEN;
																			break;
																		case "clic" :
																			enSurvol = Bouton.EnSurvol.CLIC;
																			break;
																		case "survole" :
																			enSurvol = Bouton.EnSurvol.SURVOLE;
																			break;
																	}
																	break;
																case "EnSurvolDébarque" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "reste" :
																			enSurvolDébarque = Bouton.EnSurvolDébarque.RESTE;
																			break;
																		case "clic" :
																			enSurvolDébarque = Bouton.EnSurvolDébarque.CLIC;
																			break;
																		case "retourne défaut" :
																			enSurvolDébarque = Bouton.EnSurvolDébarque.RETOURNE_DÉFAUT;
																			break;
																	}
																	break;
																case "EnSurvolRelâche" :
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "reste" :
																			enSurvolRelâche = Bouton.EnSurvolRelâche.RESTE;
																			break;
																		case "clic" :
																			enSurvolRelâche = Bouton.EnSurvolRelâche.CLIC;
																			break;
																		case "retourne défaut" :
																			enSurvolRelâche = Bouton.EnSurvolRelâche.RETOURNE_DÉFAUT;
																			break;
																	}
																	break;
																case "EnClic":
																	option = jsonSceneReader.nextString();
																	switch (option){
																		case "rien" :
																			enClic = Bouton.EnClic.RIEN;
																			break;
																		case "clic reste" :
																			enClic = Bouton.EnClic.CLIC_RESTE;
																			break;
																		case "clic immédiat" :
																			enClic = Bouton.EnClic.CLIC_IMMÉDIAT;
																			break;
																		case "alterne" :
																			enClic = Bouton.EnClic.ALTERNE;
																			break;
																	}
																	break;
																default:
																	jsonSceneReader.skipValue();
																	break;
															}
														}
														break;
													default:
														jsonSceneReader.skipValue();
														break;
												}
											}
											jsonSceneReader.endObject();

											Bouton bouton = new Bouton(bDéfaut,bPressé,bSurvol);
											bouton.changerPosition(position);
											bouton.changerRotation(rotation);
											bouton.changerTaille(taille);
											bouton.changerComportement(préréglage);
											bouton.changerComportement(enDoigtTouché,enDoigtRelâche,enDoigtDébarque,enSurvol,enSurvolDébarque,enSurvolRelâche,enClic);
											bouton.name = name;
											scene.addButton(bouton);
										}
										jsonSceneReader.endArray();
										break;
									case "Glissoires" :
										jsonSceneReader.beginArray();
										while (jsonSceneReader.hasNext()){
											jsonSceneReader.beginObject();

											String name = null;
											GUIBase barre = null;
											GUIBase bouton = null;
											Vector2f position = null;
											float rotation = 0f;
											float valMax = 0f;
											float valMin = 0f;
											float valDéfaut = 0f;
											float échelonsTaille = 0f;

											while (jsonSceneReader.hasNext()){
												String textTag = jsonSceneReader.nextName();
												switch (textTag){
													case "nom":
														name = jsonSceneReader.nextString();
														break;
													case "barre":
														String barreNom = jsonSceneReader.nextString();
														int index = guiBaseElementsNames.indexOf(barreNom);
														barre = guiBaseElements.get(index);
														break;
													case "bouton":
														String boutonNom = jsonSceneReader.nextString();
														index = guiBaseElementsNames.indexOf(boutonNom);
														bouton = guiBaseElements.get(index);
														break;
													case "position" :
														jsonSceneReader.beginArray();
														float[] p = new float[2];
														for (int i = 0; i < 2; i++) {
															p[i] = (float) jsonSceneReader.nextDouble();
														}
														jsonSceneReader.endArray();
														position = new Vector2f(p[0],p[1]);
														break;
													case "rotation" :
														rotation = (float) jsonSceneReader.nextDouble();
														break;
													case "Valeur Min":
														valMin = (float) jsonSceneReader.nextDouble();
														break;
													case "Valeur Max":
														valMax = (float) jsonSceneReader.nextDouble();
														break;
													case "Valeur Défaut" :
														valDéfaut = (float) jsonSceneReader.nextDouble();
														break;
													case "ÉchelonTaille" :
														échelonsTaille = (float) jsonSceneReader.nextDouble();
														break;
													default:
														jsonSceneReader.skipValue();
														break;
												}
											}
											jsonSceneReader.endObject();

											Glissoire glissoire = new Glissoire(barre,bouton,valMin,valMax,échelonsTaille);
											glissoire.assignerValeur(valDéfaut);
											glissoire.assignerRotation(rotation);
											glissoire.assignerPosition(position);
											glissoire.nom = name;
											scene.addGlissoire(glissoire);

										}
										jsonSceneReader.endArray();
										break;
									default:
										jsonSceneReader.skipValue();
										break;
								}
							}
							jsonSceneReader.endObject();
						}
						break;
					default:
						jsonSceneReader.skipValue();
						break;
				}
			}
			jsonSceneReader.endObject();

			ressources.feedTextures(scene,textureUsed,fontUsed);

			//ressources.log();
		}catch(Exception e){
			Log.e("loadScene", "Something went wrong");
			e.printStackTrace();
		}

		Log.w("LoadScene", (System.currentTimeMillis() - timer) +" milliseconds to load " + directory);

		return scene;
	}

	/*public void Batch(Scene scene){
		scene.CleanEntities = false;
		Log.w("Batching","Starting Batching....");
		ArrayList<Entity> entities;
		ArrayList<Entity> Ce = (ArrayList<Entity>) scene.Entities.clone();
		ArrayList<Entity> De = (ArrayList<Entity>) scene.DirtyEntities.clone();
		entities = Ce;
		ArrayList<Float> positions = new ArrayList<>();
		ArrayList<Float> uv = new ArrayList<>();
		ArrayList<Float> norm = new ArrayList<>();
		ArrayList<Integer> indices = new ArrayList<>();

		int indexOffset = 0;

		for (int l = 0; l < 2; l++) {
			for (int i = entities.size() - 1; i > -1; i--) {
				if (entities.get(i).isStatic) {
					Log.w("Batching", "Entity " + i + " on " + entities.size() + ", " + entities.get(i).model.vertCount + " vertices");
					for (int j = 0; j < entities.get(i).instanceCount; j++) {
						Log.w("Batching", "positions...");
						for (int k = 0; k < entities.get(i).model.positions.length / 3; k++) {
							Vector3f position = new Vector3f(entities.get(i).model.positions[3 * k + 0], entities.get(i).model.positions[3 * k + 1], entities.get(i).model.positions[3 * k + 2]);
							Matrix4f transform = Maths.createTransformationMatrix(entities.get(i).getPosition(j), entities.get(i).getRotation(j), entities.get(i).getScale(j));
							position = Matrix4f.MultiplyMV(transform, position);
							positions.add(position.x);
							positions.add(position.y);
							positions.add(position.z);
						}
						Log.w("Batching", "norm...");
						if (entities.get(i).model.norm != null) {
							for (int k = 0; k < entities.get(i).model.norm.length; k++) {
								norm.add(entities.get(i).model.norm[k]);
							}
						} else {
							for (int k = 0; k < entities.get(i).model.positions.length; k++) {
								norm.add(0f);
							}
						}
						Log.w("Batching", "UV...");
						if (entities.get(i).model.uv != null) {
							for (int k = 0; k < entities.get(i).model.uv.length; k++) {
								uv.add(entities.get(i).model.uv[k]);
							}
						} else {
							for (int k = 0; k < (entities.get(i).model.positions.length / 3) * 2; k++) {
								uv.add(0f);
							}
						}
						Log.w("Batching", "Indices...");
						if (entities.get(i).model.indices != null) {
							for (int k = 0; k < entities.get(i).model.indices.length; k++) {
								indices.add(entities.get(i).model.indices[k] + indexOffset);
								indexOffset += entities.get(i).model.uv.length / 2;
							}
						}
					}
					entities.remove(i);
				}
			}
			entities = De;
		}

		Log.w("Batching","Making model...");

		float[] p = new float[positions.size()];
		float[] n = new float[norm.size()];
		float[] u = new float[uv.size()];
		int[] in = new int[indices.size()];

		for (int i = 0; i < p.length; i++) {
			p[i] = positions.get(i);
		}
		for (int i = 0; i < n.length; i++) {
			n[i] = norm.get(i);
		}
		for (int i = 0; i < u.length; i++) {
			u[i] = uv.get(i);
		}
		for (int i = 0; i < in.length; i++) {
			in[i] = indices.get(i);
		}

		RawModel model = loadToVAO(p,n,u,in);

		Entity sceneEntity = new Entity(model);
		scene.Entities = Ce;
		scene.DirtyEntities = De;

		scene.addEntity(sceneEntity);

		scene.CleanEntities = true;
	}*/

	public GTexture loadAssetTexture(String dirr){
		long timer = System.currentTimeMillis();

		AssetManager assetManager = main.getAssets();
		Bitmap bmp = null;
		try {
			InputStream is = assetManager.open(dirr);
			bmp = BitmapFactory.decodeStream(is);
		}catch(Exception e){
			Log.e("AssetTexture Loader","an error occurred");
			Log.e("AssetTexture Loader",e.getMessage());
			e.printStackTrace();
		}
		Log.w("load Asset Texture", (System.currentTimeMillis()-timer) + " milliseconds to load texture : " + dirr);
		return new GTexture(bmp);
	}

	public Font loadAssetFont(String dirr, int size, int padding, int[] ranges){
		long timer = System.currentTimeMillis();

		int characterSet = 0;

		for (int i = 0; i < ranges.length; i++) {
			characterSet += ranges[i+1] - ranges[i];
			i++;
		}

		Log.w("genFont", "You want " + characterSet + " characters");

		int textureSize = (int) Math.ceil(Math.sqrt((float) characterSet))*(size+padding);

		AssetManager assets = main.getAssets();
		Typeface typeface = Typeface.createFromAsset(assets,dirr);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xffffffff);
		paint.setTextSize(size);
		paint.setTypeface(typeface);
		Bitmap bitmap = Bitmap.createBitmap(textureSize,textureSize,Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0x00000000);

		Log.w("genFont","texture size: " + textureSize + ", " + bitmap.getAllocationByteCount() + " bytes");
		Log.w("genFont", "Max texture size : " + GLES30.GL_MAX_TEXTURE_SIZE);

		float[] charWidth = new float[characterSet];
		float[] charHeight = new float[characterSet];
		float[] charDropDown = new float[characterSet];
		float[] charFarLeft = new float[characterSet];
		char a;
		int x = 0;
		int y = 0;
		float percent = 0;

		for (int j = 0; j < ranges.length; j++) {

			int rangeBegin = ranges[j];
			int rangeEnd = ranges[j+1];

			for (int i = 0; i < rangeEnd-rangeBegin; i++) {
				a = (char) (i + rangeBegin);
				canvas.drawText(a + "", 0, 1, x, y, paint);
				Rect bounds = new Rect();
				paint.getTextBounds(a + "", 0, 1, bounds);
				charWidth[i] = bounds.width();
				charHeight[i] = bounds.height();
				charDropDown[i] = bounds.bottom;
				charFarLeft[i] = bounds.left;

				x += size + padding;
				if (x + size + padding > textureSize) {
					x = 0;
					y += size + padding;
				}
				if ((float) i / (float) characterSet > 0.1f + percent) {
					percent = (float) i / (float) characterSet;
					Log.w("drawing characters", percent * 100 + "% completed");
				}
			}
			j++;
		}

		Log.w("loadAssetFont", (System.currentTimeMillis()-timer) + " milliseconds to load " + dirr);
		return new Font(new GTexture(bitmap),bitmap.getWidth(),charHeight, charWidth, charDropDown, charFarLeft, characterSet,(int) Math.ceil(Math.sqrt(characterSet)), size+padding, padding, ranges);
	}

	//TODO implement a distance field system for better character rendering.

	private static int createVAO(){
		int[] vaoID =new int[1];
		GLES30.glGenVertexArrays(1,vaoID,0);
		GLES30.glBindVertexArray(vaoID[0]);
		return  vaoID[0];
	}

	private static void storeDataInVAO(int index, int dimensions, float[] data){
		int[] vboID = new int[1];
		GLES30.glGenBuffers(1,vboID,0);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboID[0]);
		FloatBuffer floatBuffer = makeFloatBuffer(data);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,data.length*4,floatBuffer,GLES30.GL_STATIC_DRAW);
		GLES30.glVertexAttribPointer(index, dimensions, GLES30.GL_FLOAT, false, 0,0 );
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
	}

	private static void bindIndicesBuffer(int[] indices){
		int[] vboID = new int[1];
		GLES30.glGenBuffers(1,vboID,0);
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER,vboID[0]);
		IntBuffer intBuffer = makeIntBuffer(indices);
		GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,indices.length*Integer.BYTES,intBuffer,GLES30.GL_STATIC_DRAW);
	}

	private static void unbindVAO(){
		GLES30.glBindVertexArray(0);
	}

	private static FloatBuffer makeFloatBuffer(float[] array){
		FloatBuffer floatBuffer = ByteBuffer.allocateDirect(array.length*Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		floatBuffer.put(array).position(0);
		//Log.w("makeFloatBuffer", array.length * 4 +" bytes buffer created");
		return floatBuffer;
	}

	private static IntBuffer makeIntBuffer(int[] data){
		IntBuffer buffer = ByteBuffer.allocateDirect(data.length*Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
		buffer.put(data).position(0);
		return buffer;
	}
}
