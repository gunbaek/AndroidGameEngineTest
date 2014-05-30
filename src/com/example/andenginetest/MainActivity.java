package com.example.andenginetest;

import java.text.*;
import java.util.*;

import org.andengine.engine.camera.*;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.*;
import org.andengine.engine.handler.physics.*;
import org.andengine.engine.options.*;
import org.andengine.engine.options.resolutionpolicy.*;
import org.andengine.entity.scene.*;
import org.andengine.entity.scene.background.*;
import org.andengine.entity.sprite.*;
import org.andengine.entity.text.*;
import org.andengine.input.touch.*;
import org.andengine.opengl.font.*;
import org.andengine.opengl.texture.*;
import org.andengine.opengl.texture.atlas.bitmap.*;
import org.andengine.opengl.texture.atlas.bitmap.source.*;
import org.andengine.opengl.texture.region.*;
import org.andengine.opengl.vbo.*;
import org.andengine.ui.activity.*;
import org.andengine.util.*;

import android.content.*;
import android.graphics.*;
import android.util.*;


public class MainActivity extends SimpleBaseGameActivity {
	private Camera camera;
	private static int CAMERA_WIDTH = 720;
	private static int CAMERA_HEIGHT = 480;
	
	private SharedPreferences mPrefs;
	private BitmapTextureAtlas BtaJoysticBg;
	private BitmapTextureAtlas BtaSpritePlayer;
	private BitmapTextureAtlas BtaMesile;
	private BitmapTextureAtlas BtaTitle;
	
	private TiledTextureRegion TrMesile;
	private TiledTextureRegion TrJoysticBg;
	private TiledTextureRegion TrSpritePlayer;
	private TiledTextureRegion TrTitle;
	private TextureRegion TrBg;
	
	private RepeatingSpriteBackground mGrassBackground;
	private AnimatedSprite SpritePlayer;
	private AnimatedSprite SpriteTitle;
	
	private Text ScoreText;
	private Text TopScoreText;
	private Text StartText;
	
	private List<mesile> mesileList;
	private boolean isClick = false;
	public Scene scene;

	private Font mFont;
	private Font StartTextFont;
	private TimeScore TimeScoreThread;
	private Thread CreateMesileSpriteThread;
	double Score = 0;
	double TopScore = 0;
	boolean isGameStart = false;
	
	private class TimeScore extends Thread {
		long start, end;
		@Override
		public void run() {
			start = System.currentTimeMillis();
			while (isGameStart) {
				end = System.currentTimeMillis();
				Score = (Math.round(end - start) / 1000.0);
				ScoreText.setText("Score : " + Score + " Sec");
			}
		}

		public void reset() {
			if (Score > TopScore) {
				TopScore = Score;
				TopScoreText.setText("Top : " + TopScore + " Sec");
				SharedPreferences.Editor editor = mPrefs.edit();
                editor.putFloat("TopScore", (float) TopScore);
                editor.commit();
			}
		}
	}
	private class MesileThread extends Thread{
		@Override
		public synchronized void run() {
			double pos;
			float StartX, StartY;
			while (isGameStart) {
				pos = Math.random();
				if (pos >= 3 / 4) {
					// top
					StartX = (float) (Math.random() * CAMERA_WIDTH);
					StartY = 0;
				} else if (pos >= 2 / 4) {
					// btm
					StartX = (float) (Math.random() * CAMERA_WIDTH);
					StartY = CAMERA_HEIGHT;
				} else if (pos >= 1 / 4) {
					// right
					StartX = 0;
					StartY = (float) (Math.random() * CAMERA_HEIGHT);
				} else {
					// left
					StartX = CAMERA_WIDTH;
					StartY = (float) (Math.random() * CAMERA_HEIGHT);
				}
				mesile tempMesile = new mesile(StartX, StartY, 30, 30,
						TrMesile, getVertexBufferObjectManager());
				tempMesile.animate(new long[] { 200, 200, 200, 200, 200,
						200 }, 0, 5, true);
				mesileList.add(tempMesile);
				try {
					Thread.sleep(1000);
					if(!isGameStart)
						clearMesile();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				scene.attachChild(tempMesile);
			}
		}
	}
	private class mesile extends AnimatedSprite {
		private PhysicsHandler mPhysicsHandler;
		float MoveX = (float) (Math.random() * 300);
		float MoveY = (float) (Math.random() * 300);

		public mesile(float pX, float pY, float pWidth, float pHeight,
				ITiledTextureRegion pTiledTextureRegion,
				VertexBufferObjectManager pTiledSpriteVertexBufferObject) {
			super(pX, pY, pWidth, pHeight, pTiledTextureRegion,
					pTiledSpriteVertexBufferObject);
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			if (Math.random() >= 0.5)
				MoveX = MoveX * -1;
			if (Math.random() >= 0.5)
				MoveY = MoveY * -1;
			this.mPhysicsHandler.setVelocity(MoveX, MoveY);
		}

		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			if (this.mX < 0) {
				if (MoveX < 0) {
					MoveX = -MoveX;
				}
				this.mPhysicsHandler.setVelocityX(MoveX);
			} else if (this.mX + this.getWidth() > CAMERA_WIDTH) {
				if (MoveX > 0) {
					MoveX = -MoveX;
				}
				this.mPhysicsHandler.setVelocityX(MoveX);
			}
			if (this.mY < 0) {
				if (MoveY < 0) {
					MoveY = -MoveY;
				}
				this.mPhysicsHandler.setVelocityY(MoveY);
			} else if (this.mY + this.getHeight() > CAMERA_HEIGHT) {
				if (MoveY > 0) {
					MoveY = -MoveY;
				}
				this.mPhysicsHandler.setVelocityY(MoveY);
			}
			super.onManagedUpdate(pSecondsElapsed);
		}

		public void collide() {
			if (this.collidesWith(SpritePlayer)) {
				Iterator<mesile> itr = mesileList.iterator();
				while (itr.hasNext()) {
					scene.detachChild(itr.next());
				}
				mesileList.clear();
			}
		}
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(),
				camera);
		return engineOptions;
	}
	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		//SpritePlayer 생성
		this.BtaSpritePlayer = new BitmapTextureAtlas(
				this.getTextureManager(), 2048, 512, TextureOptions.BILINEAR);
		this.TrSpritePlayer = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.BtaSpritePlayer, this,
						"player.png", 0, 0, 4, 1);
		this.BtaSpritePlayer.load();

		// Joystic 생성
		BtaJoysticBg = new BitmapTextureAtlas(getTextureManager(), 2, 2,
				TextureOptions.BILINEAR);
		TrJoysticBg = new BitmapTextureAtlasTextureRegionFactory()
				.createTiledFromAsset(BtaJoysticBg, this, "tr.png", 0, 0, 1, 1);

		// BG 생성
		this.mGrassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH,
				CAMERA_HEIGHT, this.getTextureManager(),
				AssetBitmapTextureAtlasSource.create(this.getAssets(),
						"gfx/bg.png"), this.getVertexBufferObjectManager());

		// Mesile 생성
		BtaMesile = new BitmapTextureAtlas(getTextureManager(), 512, 256,
				TextureOptions.BILINEAR);
		TrMesile = new BitmapTextureAtlasTextureRegionFactory()
				.createTiledFromAsset(BtaMesile, this, "mesile.png", 0, 0, 4, 2);

		// font 생성
		mFont = FontFactory.create(this.getFontManager(),
				this.getTextureManager(), 256, 256,
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32,
				Color.WHITE);
		StartTextFont = FontFactory.create(this.getFontManager(),
				this.getTextureManager(), 256, 256,
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 50,
				Color.WHITE);
		// Title 생성
		BtaTitle = new BitmapTextureAtlas(getTextureManager(), 512, 256,
				TextureOptions.BILINEAR);
		TrTitle = new BitmapTextureAtlasTextureRegionFactory()
		.createTiledFromAsset(BtaTitle, this, "title.png", 0, 0, 1, 1);
		
		StartTextFont.load();
		mFont.load();
		BtaMesile.load();
		BtaJoysticBg.load();
		BtaTitle.load();
		
		mPrefs = getSharedPreferences("TopScore",android.content.Context.MODE_PRIVATE);
    	TopScore = mPrefs.getFloat("TopScore", 0);
    	Log.i("TopScore", "TopScore : " + TopScore);
	}
	@Override
	protected Scene onCreateScene() {
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

		final float SpritePlayerX = (CAMERA_WIDTH - this.TrSpritePlayer.getWidth()) / 2;
		final float SpritePlayerY = CAMERA_HEIGHT	- this.TrSpritePlayer.getHeight() - 5;
		final float JoysticBgWidth = CAMERA_WIDTH;
		final float JoysticBgHeight = CAMERA_HEIGHT;
		final float JoysticBgMerginX = 0;
		final float JoysticBgMerginY = 0;
		final float JoysticBgLeft = CAMERA_WIDTH - JoysticBgWidth - JoysticBgMerginX;
		final float JoysticBgRight = CAMERA_WIDTH - JoysticBgMerginX;
		final float JoysticBgTop = CAMERA_HEIGHT - JoysticBgMerginY	- JoysticBgHeight;
		final float JoysticBgBottum = CAMERA_HEIGHT - JoysticBgMerginY;

		scene = new Scene();
		DecimalFormat t = new DecimalFormat("#.000");
		ScoreText = new Text(40, 40, mFont, "Score : 0", 50, new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
		TopScoreText = new Text(40, 80, mFont, "Top : "+t.format(TopScore)+"Sec", 50, new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
		StartText = new Text(0,0, StartTextFont, "Push to Start", 50, new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);

		//Title Sprite
		SpriteTitle = new AnimatedSprite(0, 0, 512, 256, TrTitle, vertexBufferObjectManager);
		SpriteTitle.setPosition((CAMERA_WIDTH - SpriteTitle.getWidth())/2, (CAMERA_HEIGHT - SpriteTitle.getHeight())/2);
		StartText.setPosition((CAMERA_WIDTH - StartText.getWidth())/2, CAMERA_HEIGHT/2 + SpriteTitle.getHeight()/2 + 30);

		//SpritePlayer Sprite
		SpritePlayer = new AnimatedSprite(SpritePlayerX, SpritePlayerY, 40, 40, this.TrSpritePlayer, vertexBufferObjectManager);
		SpritePlayer.setScaleCenterY(this.TrSpritePlayer.getHeight());
		SpritePlayer.animate(new long[] { 200, 200, 200, 200 }, 0, 3, true);
		
		//JoysticBg Sprite
		Sprite SpriteJoysticBg = new Sprite(JoysticBgLeft, JoysticBgTop,
				JoysticBgWidth, JoysticBgHeight, TrJoysticBg,
				vertexBufferObjectManager) {
			float MoveX = 0;
			float MoveY = 0;
			float PreMoveX = 0;
			float PreMoveY = 0;

			int minX = 0;
			int maxX = (int) getEngine().getCamera().getWidth()
					- (int) SpritePlayer.getWidth();
			int minY = 0;
			int maxY = (int) getEngine().getCamera().getHeight()
					- (int) SpritePlayer.getHeight();

			@Override
			public boolean onAreaTouched(final TouchEvent TouchE,
					final float TouchX, final float TouchY) {
				// Log.i("SpriteJoysticBg","Touch X:"+TouchX+" Y:"+TouchY);

				
				if(!isGameStart && !isClick){
					GameStart();
				}else{
					if (PreMoveX != 0) {
						MoveX = (float) ((TouchX - PreMoveX) * 0.8);
						MoveY = (float) ((TouchY - PreMoveY) * 0.8);
					}
					PreMoveX = TouchX;
					PreMoveY = TouchY;
		
					// SpriteJoysticPoint.setPosition(JoysticBgLeft + TouchX -
					// SpriteJoysticPoint.getWidth()/2,JoysticBgTop + TouchY -
					// SpriteJoysticPoint.getHeight()/2);
					// SpriteJoysticPoint.setVisible(true);

					if (TouchE.getAction() == TouchEvent.ACTION_UP) {
						// SpriteJoysticPoint.setVisible(false);
						PreMoveX = 0;
						PreMoveY = 0;
						MoveX = 0;
						MoveY = 0;
					}
				}
				if(TouchE.getAction() == TouchEvent.ACTION_DOWN){
					isClick = true;
				}else if(TouchE.getAction() == TouchEvent.ACTION_UP){
					isClick = false;
				}
				return false;
			}

			@Override
			protected void onManagedUpdate(float pSecondsElapsed) {
				if (isClick) {
					if (SpritePlayer.getX() + MoveX < minX) {
						SpritePlayer.setPosition(minX, SpritePlayer.getY());
					} else if (SpritePlayer.getX() + MoveX > maxX) {
						SpritePlayer.setPosition(maxX, SpritePlayer.getY());
					} else if (SpritePlayer.getX() >= minX && SpritePlayer.getX() <= maxX) {
						SpritePlayer.setPosition(SpritePlayer.getX() + MoveX, SpritePlayer.getY());
					}
					if (SpritePlayer.getY() + MoveY < minY) {
						SpritePlayer.setPosition(SpritePlayer.getX(), minY);
					} else if (SpritePlayer.getY() + MoveY > maxY) {
						SpritePlayer.setPosition(SpritePlayer.getX(), maxY);
					} else if (SpritePlayer.getY() >= minY && SpritePlayer.getY() <= maxY) {
						SpritePlayer.setPosition(SpritePlayer.getX(), SpritePlayer.getY() + MoveY);
					}
				}

				super.onManagedUpdate(pSecondsElapsed);
			};
		};
		mesileList = new ArrayList<mesile>();
		
		//CreateMesileSpriteThread 
		CreateMesileSpriteThread = new MesileThread();
		TimeScoreThread = new TimeScore();
		
		scene.registerTouchArea(SpriteJoysticBg);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.setBackground(this.mGrassBackground);
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {	}

			@Override
			public void onUpdate(final float pSecondsElapsed) {

				int ListSize = mesileList.size() - 1;
				for (int i = 0; i < ListSize; i++) {
					if (mesileList.get(i).collidesWith(SpritePlayer)) {
						Log.e("Collide", "!!");
						GameStop();
						break;
					}
				}
			}
		});
		scene.attachChild(SpriteTitle);
		scene.attachChild(StartText);
		scene.attachChild(SpritePlayer);
		scene.attachChild(SpriteJoysticBg);
		scene.attachChild(ScoreText);
		scene.attachChild(TopScoreText);
		
		GameStop();
		return scene;
	}
	public void GameStop(){
		isGameStart = false;
		TimeScoreThread.reset();
		SpritePlayer.setVisible(false);
		SpriteTitle.setVisible(true);
		StartText.setVisible(true);
		clearMesile();
	}
	public void GameStart(){
		clearMesile();
		isGameStart = true;
		SpritePlayer.setVisible(true);
		SpriteTitle.setVisible(false);
		StartText.setVisible(false);
		TimeScoreThread = new TimeScore();
		TimeScoreThread.start();
		CreateMesileSpriteThread = new MesileThread();
		CreateMesileSpriteThread.start();
	}
	public synchronized void clearMesile(){
		for (int j = 0; j < mesileList.size(); j++) {
			scene.detachChild(mesileList.get(0));
			mesileList.remove(0);
		}
	}
}