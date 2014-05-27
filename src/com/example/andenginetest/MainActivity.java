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
 private static final int CAMERA_WIDTH = 720;
 private static final int CAMERA_HEIGHT = 480;
 
 private BitmapTextureAtlas BtaJoysticBg;
 private BitmapTextureAtlas BtaJoysticPoint;
 
 private BitmapTextureAtlas mBitmapTextureAtlas;
 private BitmapTextureAtlas BtaBtnMoveR;
 private BitmapTextureAtlas BtaBtnMoveL;
 private BitmapTextureAtlas BtaBtnMoveU;
 private BitmapTextureAtlas BtaBtnMoveD;
 
 private TiledTextureRegion TrJoysticBg;
 private TiledTextureRegion TrJoysticPoint;
 private TiledTextureRegion mPlayerTextureRegion;
 private TiledTextureRegion TrBtnMoveR;
 private TiledTextureRegion TrBtnMoveL;
 private TiledTextureRegion TrBtnMoveU;
 private TiledTextureRegion TrBtnMoveD;
 
 private boolean isMoveR = false;
 private boolean isMoveL = false;
 private boolean isMoveU = false;
 private boolean isMoveD = false;
 private boolean isClick = false;
 
 private Sprite btnMoveR, btnMoveL, btnMoveU, btnMoveD;
 
 @Override
 public EngineOptions onCreateEngineOptions()
 {
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
     
     //버튼 BTA 생성
     BtaBtnMoveD =  new BitmapTextureAtlas(getTextureManager(), 500, 250, TextureOptions.BILINEAR);
     BtaBtnMoveR =  new BitmapTextureAtlas(getTextureManager(), 500, 250, TextureOptions.BILINEAR);
     BtaBtnMoveL =  new BitmapTextureAtlas(getTextureManager(), 500, 250, TextureOptions.BILINEAR);
     BtaBtnMoveU =  new BitmapTextureAtlas(getTextureManager(), 500, 250, TextureOptions.BILINEAR);
     
     //버튼 TR 생성
     TrBtnMoveD = new BitmapTextureAtlasTextureRegionFactory()
     .createTiledFromAsset(BtaBtnMoveD, this, "btnDown.png", 0, 0, 2, 1);
     TrBtnMoveR = new BitmapTextureAtlasTextureRegionFactory()
     .createTiledFromAsset(BtaBtnMoveR, this, "btnRight.png", 0,0, 2, 1);
     TrBtnMoveL = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaBtnMoveL, this, "btnLeft.png", 0,0, 2, 1);
     TrBtnMoveU = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaBtnMoveU, this, "btnUp.png", 0,0, 2, 1);
     
     BtaBtnMoveD.load();
     BtaBtnMoveL.load();
     BtaBtnMoveR.load();
     BtaBtnMoveU.load();
     
     //Joystic 생성
     BtaJoysticBg = new BitmapTextureAtlas(getTextureManager(), 500, 500, TextureOptions.BILINEAR);
     BtaJoysticPoint = new BitmapTextureAtlas(getTextureManager(), 50, 50, TextureOptions.BILINEAR);
     TrJoysticBg = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaJoysticBg, this, "joysticBg.png", 0, 0, 1,1);
     TrJoysticPoint = new BitmapTextureAtlasTextureRegionFactory().createTiledFromAsset(BtaJoysticPoint, this, "joysticPoint.png", 0, 0, 1,1);
     
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


  //btn setting
  Sprite SpriteBtnMoveU = new Sprite(100, CAMERA_HEIGHT - 220, 80, 80, TrBtnMoveU, vertexBufferObjectManager){
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		 if(TouchE.getAction() == TouchEvent.ACTION_DOWN){
			 isClick = true;
			 isMoveU = true;
			 Log.e("btnClick", "U_DOWN");
		 }else if(TouchE.getAction() == TouchEvent.ACTION_UP){
			 isClick = false;
			 isMoveU = false;;
			 Log.e("btnClick", "U_UP");
		 }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {
		  int minY = 0;
		  int maxY = (int)getEngine().getCamera().getHeight();
		  
		  if(isClick && isMoveU && player.getY()-player.getHeight() >= minY){
			  Log.e("onManagedUpdate", "U");
			  player.setPosition(player.getX(), player.getY()-10);
		  }
		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };
  Sprite SpriteBtnMoveD = new Sprite(100, CAMERA_HEIGHT - 100, 80, 80, TrBtnMoveD, vertexBufferObjectManager){
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		 if(TouchE.getAction() == TouchEvent.ACTION_DOWN){
			 isClick = true;
			 isMoveD = true;
			 Log.e("btnClick", "D_DOWN");
		 }else if(TouchE.getAction() == TouchEvent.ACTION_UP){
			 isClick = false;
			 isMoveD = false;;
			 Log.e("btnClick", "D_UP");
		 }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {
		  int minY = 0;
		  int maxY = (int)getEngine().getCamera().getHeight();
		 
		  if(isClick && isMoveD && player.getY()+player.getHeight() <= maxY){
			  Log.e("onManagedUpdate", "D");
			  player.setPosition(player.getX(), player.getY()+10);
		  }
		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };
  Sprite SpriteBtnMoveL = new Sprite(20, CAMERA_HEIGHT - 160, 80, 80, TrBtnMoveL, vertexBufferObjectManager){
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		 if(TouchE.getAction() == TouchEvent.ACTION_DOWN){
			 isClick = true;
			 isMoveL = true;
			 Log.e("btnClick", "L_DOWN");
		 }else if(TouchE.getAction() == TouchEvent.ACTION_UP){
			 isClick = false;
			 isMoveL = false;
			 Log.e("btnClick", "L_UP");
		 }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {
		  int minX = 0;
		  int maxX = (int)getEngine().getCamera().getWidth();
		  
		  if(isClick && isMoveL && player.getX()-player.getWidth() >= minX){
			  Log.e("onManagedUpdate", "L");
			  player.setPosition(player.getX()-10, player.getY());
		  }
		  
		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };
  Sprite SpriteBtnMoveR = new Sprite(180, CAMERA_HEIGHT - 160, 80, 80, TrBtnMoveR, vertexBufferObjectManager){
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		 if(TouchE.getAction() == TouchEvent.ACTION_DOWN){
			 isClick = true;
			 isMoveR = true;
			 Log.e("btnClick", "R_DOWN");
		 }else if(TouchE.getAction() == TouchEvent.ACTION_UP){
			 isClick = false;
			 isMoveR = false;;
			 Log.e("btnClick", "R_UP");
		 }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {
		  int minX = 0;
		  int maxX = (int)getEngine().getCamera().getWidth() - (int)player.getWidth();
		  
		  if(isClick && isMoveR && player.getX()+player.getWidth() <= maxX){
			  Log.e("onManagedUpdate", "R");
			  player.setPosition(player.getX()+10, player.getY());
		  }
		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };

  final float JoysticBgWidth = 200;
  final float JoysticBgHeight = 200;
  final float JoysticBgMerginX = 100;
  final float JoysticBgMerginY = 100;
  final float JoysticBgLeft = CAMERA_WIDTH - JoysticBgWidth - JoysticBgMerginX;
  final float JoysticBgRight = CAMERA_WIDTH - JoysticBgMerginX; 
  final float JoysticBgTop = CAMERA_HEIGHT - JoysticBgMerginY - JoysticBgHeight;
  final float JoysticBgButtom = CAMERA_HEIGHT - JoysticBgMerginY ;
  final Sprite SpriteJoysticPoint = new Sprite(0,0,25,25,TrJoysticPoint, vertexBufferObjectManager);
  
  Sprite SpriteJoysticBg = new Sprite(JoysticBgLeft, JoysticBgTop, JoysticBgWidth, JoysticBgHeight, TrJoysticBg, vertexBufferObjectManager){
	  @Override
	  public boolean onAreaTouched(final TouchEvent TouchE, final float TouchX, final float TouchY) {
		  if(TouchX >= JoysticBgLeft && TouchX <= JoysticBgRight 
				  && TouchY >= JoysticBgTop && TouchY <= JoysticBgButtom){
			  SpriteJoysticPoint.setPosition(TouchY - SpriteJoysticPoint.getWidth()/2, TouchY - SpriteJoysticPoint.getHeight()/2);
			  SpriteJoysticPoint.setVisible(true);
			  
		  }
		  return false;
	  }  
	  @Override
	  protected void onManagedUpdate(float pSecondsElapsed) {

		  super.onManagedUpdate(pSecondsElapsed);
	  };
  };
 
  
  scene.attachChild(SpriteBtnMoveR);
  scene.attachChild(SpriteBtnMoveL);
  scene.attachChild(SpriteBtnMoveU);
  scene.attachChild(SpriteBtnMoveD);
  
  scene.registerTouchArea(SpriteBtnMoveR);
  scene.registerTouchArea(SpriteBtnMoveL);
  scene.registerTouchArea(SpriteBtnMoveU);
  scene.registerTouchArea(SpriteBtnMoveD);

  SpriteJoysticPoint.setVisible(false);
  scene.attachChild(SpriteJoysticPoint);
  scene.attachChild(SpriteJoysticBg);
  scene.registerTouchArea(SpriteJoysticBg);
  scene.setTouchAreaBindingOnActionDownEnabled(true);
  
  
  return scene;
 }
}