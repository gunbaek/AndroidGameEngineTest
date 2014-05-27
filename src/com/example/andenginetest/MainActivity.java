package com.example.andenginetest;

import org.andengine.engine.camera.*;
import org.andengine.engine.options.*;
import org.andengine.engine.options.resolutionpolicy.*;
import org.andengine.entity.scene.*;
import org.andengine.entity.scene.background.*;
import org.andengine.entity.sprite.*;
import org.andengine.input.touch.*;
import org.andengine.opengl.texture.*;
import org.andengine.opengl.texture.atlas.bitmap.*;
import org.andengine.opengl.texture.region.*;
import org.andengine.opengl.vbo.*;
import org.andengine.ui.activity.*;

import android.util.*;
public class MainActivity extends SimpleBaseGameActivity
{
 // 카메라 필드를 생성합니다.
 // 가로와 세로 2개의 카메라 변수를 선언했습니다.
 private Camera camera;
 private static int CAMERA_WIDTH = 720;
 private static int CAMERA_HEIGHT = 480;
 
 private BitmapTextureAtlas BtaJoysticBg;
 private BitmapTextureAtlas BtaJoysticPoint;
 private BitmapTextureAtlas mBitmapTextureAtlas;
 
 private TiledTextureRegion TrJoysticBg;
 private TiledTextureRegion TrJoysticPoint;
 private TiledTextureRegion mPlayerTextureRegion;

 private boolean isClick = false;
 
 @Override
 public EngineOptions onCreateEngineOptions()
 {
	 DisplayMetrics displayMetrics = new DisplayMetrics();
	 getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	 CAMERA_WIDTH = displayMetrics.widthPixels;
	 CAMERA_HEIGHT = displayMetrics.heightPixels;
  camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
  EngineOptions engineOptions = new EngineOptions(true,
    ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), camera);
  return engineOptions;
 }
 
 @Override
 protected void onCreateResources()
 {
	 BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	 this.mBitmapTextureAtlas = 
			 new BitmapTextureAtlas(this.getTextureManager(), 90, 30, TextureOptions.BILINEAR);
	 this.mPlayerTextureRegion = 
			 BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset
			 (this.mBitmapTextureAtlas, this, "player.png", 0, 0, 3, 1);
     this.mBitmapTextureAtlas.load();
        
     //Joystic 생성
     BtaJoysticBg = new BitmapTextureAtlas(getTextureManager(), 500, 500, TextureOptions.BILINEAR);
     BtaJoysticPoint = new BitmapTextureAtlas(getTextureManager(), 50, 50, TextureOptions.BILINEAR);
     TrJoysticBg = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaJoysticBg, this, "joysticBg.png", 0, 0, 1,1);
     TrJoysticPoint = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaJoysticPoint, this, "joysticPoint.png", 0, 0, 1,1);
     
     BtaJoysticBg.load();
     BtaJoysticPoint.load();
     
 }
 
// onCreateScene()에서는 화면에 관련된 scene object를 초기화 합니다.
// Scene이란 스프라이트와 같은 새로운 객체를 덧붙일수 있는 객체(Entity)입니다.
// 배경 화면은 파랑(blue)로 셋팅하였습니다.
 
 @Override
 protected Scene onCreateScene()
 {
  final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
 
  Scene scene = new Scene();
  scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
 
  final float playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getWidth()) / 2;
  final float playerY = CAMERA_HEIGHT - this.mPlayerTextureRegion.getHeight() - 5;
  final AnimatedSprite player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion, vertexBufferObjectManager);
  player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
  player.setScale(2);
  player.animate(new long[]{200, 200, 200}, 0, 2, true);
  scene.attachChild(player);

  final float JoysticBgWidth = 400;
  final float JoysticBgHeight = 400;
  final float JoysticBgMerginX = 100;
  final float JoysticBgMerginY = 100;
  final float JoysticBgLeft = CAMERA_WIDTH - JoysticBgWidth - JoysticBgMerginX;
  final float JoysticBgRight = CAMERA_WIDTH - JoysticBgMerginX; 
  final float JoysticBgTop = CAMERA_HEIGHT - JoysticBgMerginY - JoysticBgHeight;
  final float JoysticBgBottum = CAMERA_HEIGHT - JoysticBgMerginY ;
  final Sprite SpriteJoysticPoint = new Sprite(0,0,50,50,TrJoysticPoint, vertexBufferObjectManager);

  Sprite SpriteJoysticBg = new Sprite(JoysticBgLeft, JoysticBgTop, JoysticBgWidth, JoysticBgHeight, TrJoysticBg, vertexBufferObjectManager){
	  float MoveX = 0;
	  float MoveY = 0;
	  int minX = 0;
	  int maxX = (int)getEngine().getCamera().getWidth() - (int)player.getWidth();
	  int minY = 0;
	  int maxY = (int)getEngine().getCamera().getHeight();
	  
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		 	 Log.i("SpriteJoysticBg","Touch X:"+TouchX+" Y:"+TouchY);

			  MoveX = (TouchX - (JoysticBgWidth/2))/10;
			  MoveY = (TouchY - (JoysticBgHeight/2))/10;
		
		 	 SpriteJoysticPoint.setPosition(JoysticBgLeft + TouchX - SpriteJoysticPoint.getWidth()/2,JoysticBgTop + TouchY - SpriteJoysticPoint.getHeight()/2);
			 SpriteJoysticPoint.setVisible(true);
			 isClick = true;
		 	 if(TouchE.getAction() == TouchEvent.ACTION_UP){
				  SpriteJoysticPoint.setVisible(false);
				  isClick = false;
			 }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {
		  if(isClick){
			  if(player.getX() + MoveX < minX){
				  player.setPosition(minX, player.getY());
			  }else if(player.getX() + MoveX > maxX){
				  player.setPosition(maxX, player.getY());
			  }else if(player.getX() >= minX && player.getX() <= maxX){
				  player.setPosition(player.getX() + MoveX, player.getY());
			  }
			  if(player.getY() + MoveY < minY){
				  player.setPosition(player.getX(), minY);
			  }else if(player.getY() + MoveY > maxY){
				  player.setPosition(player.getX(), maxY);
			  }else if(player.getY() >= minY && player.getY() <= maxY){
				  player.setPosition(player.getX(), player.getY()+MoveY);
			  }
		  }
		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };
 

  scene.attachChild(SpriteJoysticBg);
  scene.attachChild(SpriteJoysticPoint);

  SpriteJoysticPoint.setZIndex(5);
  SpriteJoysticBg.setZIndex(0);
  
  scene.registerTouchArea(SpriteJoysticBg);
  SpriteJoysticPoint.setVisible(false);
  scene.setTouchAreaBindingOnActionDownEnabled(true);
  
  
  return scene;
 }
}