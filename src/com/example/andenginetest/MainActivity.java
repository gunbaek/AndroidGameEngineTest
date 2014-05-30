package com.example.andenginetest;

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

import android.graphics.*;
import android.util.*;

public class MainActivity extends SimpleBaseGameActivity {
	private Camera camera;
	private static int CAMERA_WIDTH = 720;
	private static int CAMERA_HEIGHT = 480;

	private BitmapTextureAtlas BtaJoysticBg;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas BtaMesile;

	private TiledTextureRegion TrMesile;
	private TiledTextureRegion TrJoysticBg;
	private TiledTextureRegion mPlayerTextureRegion;
	private TextureRegion TrBg;
	private RepeatingSpriteBackground mGrassBackground;
	AnimatedSprite player;

	private Text ScoreText;
	private Text TopScoreText;

	private List<mesile> mesileList;
	private boolean isClick = false;
	public Scene scene;

	private Font mFont;
	private TimeScore TimeScoreThread;

	private class TimeScore extends Thread {
		long start, end;
		double Score = 0;
		double TopScore = 0;

		@Override
		public void run() {
			start = System.currentTimeMillis();
			while (true) {
				end = System.currentTimeMillis();
				Score = (Math.round(end - start) / 1000.0);
				ScoreText.setText("Score : " + Score + " Sec");
			}
		}

		public void reset() {
			if (Score > TopScore) {
				TopScore = Score;
				TopScoreText.setText("Top : " + TopScore + " Sec");
			}
			start = System.currentTimeMillis();
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
			if (this.collidesWith(player)) {
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
		//Player 생성
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 2048, 512, TextureOptions.BILINEAR);
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"player.png", 0, 0, 4, 1);
		this.mBitmapTextureAtlas.load();

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
		mFont.load();

		BtaMesile.load();
		BtaJoysticBg.load();

	}
	@Override
	protected Scene onCreateScene() {
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

		final float playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getWidth()) / 2;
		final float playerY = CAMERA_HEIGHT	- this.mPlayerTextureRegion.getHeight() - 5;
		final float JoysticBgWidth = CAMERA_WIDTH;
		final float JoysticBgHeight = CAMERA_HEIGHT;
		final float JoysticBgMerginX = 0;
		final float JoysticBgMerginY = 0;
		final float JoysticBgLeft = CAMERA_WIDTH - JoysticBgWidth - JoysticBgMerginX;
		final float JoysticBgRight = CAMERA_WIDTH - JoysticBgMerginX;
		final float JoysticBgTop = CAMERA_HEIGHT - JoysticBgMerginY	- JoysticBgHeight;
		final float JoysticBgBottum = CAMERA_HEIGHT - JoysticBgMerginY;

		scene = new Scene();
		ScoreText = new Text(40, 40, mFont, "Score : 0", 50, new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
		TopScoreText = new Text(40, 80, mFont, "Top : 0", 50, new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
		
		//player Sprite
		player = new AnimatedSprite(playerX, playerY, 40, 40, this.mPlayerTextureRegion, vertexBufferObjectManager);
		player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
		player.animate(new long[] { 200, 200, 200, 200 }, 0, 3, true);
		
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
					- (int) player.getWidth();
			int minY = 0;
			int maxY = (int) getEngine().getCamera().getHeight()
					- (int) player.getHeight();

			@Override
			public boolean onAreaTouched(final TouchEvent TouchE,
					final float TouchX, final float TouchY) {
				// Log.i("SpriteJoysticBg","Touch X:"+TouchX+" Y:"+TouchY);

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
				isClick = true;
				if (TouchE.getAction() == TouchEvent.ACTION_UP) {
					// SpriteJoysticPoint.setVisible(false);
					PreMoveX = 0;
					PreMoveY = 0;
					MoveX = 0;
					MoveY = 0;
				}
				return false;
			}

			@Override
			protected void onManagedUpdate(float pSecondsElapsed) {
				if (isClick) {
					if (player.getX() + MoveX < minX) {
						player.setPosition(minX, player.getY());
					} else if (player.getX() + MoveX > maxX) {
						player.setPosition(maxX, player.getY());
					} else if (player.getX() >= minX && player.getX() <= maxX) {
						player.setPosition(player.getX() + MoveX, player.getY());
					}
					if (player.getY() + MoveY < minY) {
						player.setPosition(player.getX(), minY);
					} else if (player.getY() + MoveY > maxY) {
						player.setPosition(player.getX(), maxY);
					} else if (player.getY() >= minY && player.getY() <= maxY) {
						player.setPosition(player.getX(), player.getY() + MoveY);
					}
				}

				super.onManagedUpdate(pSecondsElapsed);
			};
		};
		mesileList = new ArrayList<mesile>();
		
		//CreateMesileSpriteThread 
		Thread CreateMesileSpriteThread = new Thread(new Runnable() {
			@Override
			public void run() {
				double pos;
				float StartX, StartY;
				while (true) {
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
							TrMesile, vertexBufferObjectManager);
					tempMesile.animate(new long[] { 200, 200, 200, 200, 200,
							200 }, 0, 5, true);
					mesileList.add(tempMesile);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					scene.attachChild(tempMesile);
				}
			}
		});
		TimeScoreThread = new TimeScore();
		
		scene.registerTouchArea(SpriteJoysticBg);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.setBackground(this.mGrassBackground);
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {

				int ListSize = mesileList.size() - 1;
				for (int i = 0; i < ListSize; i++) {
					if (mesileList.get(i).collidesWith(player)) {
						Log.e("Collide", "!!");
						for (int j = 0; j < ListSize; j++) {
							scene.detachChild(mesileList.get(0));
							mesileList.remove(0);
						}
						TimeScoreThread.reset();
						break;
					}
				}

			}
		});
		
		scene.attachChild(SpriteJoysticBg);
		scene.attachChild(player);
		scene.attachChild(ScoreText);
		scene.attachChild(TopScoreText);
		
		CreateMesileSpriteThread .start();
		TimeScoreThread.start();
		return scene;
	}

}