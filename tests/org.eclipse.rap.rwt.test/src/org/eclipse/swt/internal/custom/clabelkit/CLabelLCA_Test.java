/*******************************************************************************
 * Copyright (c) 2009, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.custom.clabelkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.rap.rwt.internal.protocol.ProtocolTestUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("deprecation")
public class CLabelLCA_Test {

  private Display display;
  private Shell shell;
  private CLabelLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    lca = new CLabelLCA();
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  /*
   * 280291: [CLabel] causes NullPointerException when rendered uninitialized
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=280291
   */
  @Test
  public void testWriteText() throws IOException {
    CLabel label = new CLabel( shell, SWT.NONE );
    assertNull( label.getText() ); // assert precondition: text == null

    lca.renderChanges( label );
    // the purpose of this test is to ensure that the LCA works without throwing
    // an exception - thus there is no assert
  }

  @Test
  public void testRenderCreate() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.renderInitialization( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertEquals( "rwt.widgets.Label", operation.getType() );
    assertFalse( operation.getPropertyNames().contains( "markupEnabled" ) );
  }

  @Test
  public void testRenderCreateWithMarkupEnabled() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    clabel.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    lca.renderInitialization( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertEquals( Boolean.TRUE, operation.getProperty( "markupEnabled" ) );
  }

  @Test
  public void testRenderCreateWithShadowIn() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.SHADOW_IN );

    lca.renderInitialization( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertEquals( "rwt.widgets.Label", operation.getType() );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "SHADOW_IN" ) );
  }

  @Test
  public void testRenderCreateWithAlignment() throws Exception {
    CLabel clabel = new CLabel( shell, SWT.CENTER );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "center", message.findCreateProperty( clabel, "alignment" ) );
  }

  @Test
  public void testRenderParent() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.renderInitialization( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertEquals( WidgetUtil.getId( clabel.getParent() ), operation.getParent() );
  }

  @Test
  public void testRenderInitialText() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  @Test
  public void testRenderText() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setText( "foo" );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( clabel, "text" ) );
  }

  @Test
  public void testRenderTextUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "text" ) );
  }

  @Test
  public void testRenderInitialImage() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "image" ) == -1 );
  }

  @Test
  public void testRenderImage() throws IOException, JSONException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Image image = Graphics.getImage( Fixture.IMAGE_100x50 );

    clabel.setImage( image );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JSONArray actual = ( JSONArray )message.findSetProperty( clabel, "image" );
    assertTrue( ProtocolTestUtil.jsonEquals( expected, actual ) );
  }

  @Test
  public void testRenderImageUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );
    Image image = Graphics.getImage( Fixture.IMAGE_100x50 );

    clabel.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "image" ) );
  }

  @Test
  public void testRenderImageReset() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );
    Image image = Graphics.getImage( Fixture.IMAGE_100x50 );
    clabel.setImage( image );

    Fixture.preserveWidgets();
    clabel.setImage( null );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( clabel, "image" ) );
  }

  @Test
  public void testRenderInitialAlignment() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "alignment" ) == -1 );
  }

  @Test
  public void testRenderAlignment() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setAlignment( SWT.RIGHT );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "right", message.findSetProperty( clabel, "alignment" ) );
  }

  @Test
  public void testRenderAlignmentUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setAlignment( SWT.RIGHT );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "alignment" ) );
  }

  @Test
  public void testRenderInitialLeftMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "leftMargin" ) == -1 );
  }

  @Test
  public void testRenderLeftMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setLeftMargin( 5 );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( new Integer( 5 ), message.findSetProperty( clabel, "leftMargin" ) );
  }

  @Test
  public void testRenderLeftMarginUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setLeftMargin( 5 );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "leftMargin" ) );
  }


  @Test
  public void testRenderInitialTopMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "topMargin" ) == -1 );
  }

  @Test
  public void testRenderTopMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setTopMargin( 5 );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( new Integer( 5 ), message.findSetProperty( clabel, "topMargin" ) );
  }

  @Test
  public void testRenderTopMarginUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setTopMargin( 5 );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "topMargin" ) );
  }

  @Test
  public void testRenderInitialRightMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "rightMargin" ) == -1 );
  }

  @Test
  public void testRenderRightMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setRightMargin( 5 );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( new Integer( 5 ), message.findSetProperty( clabel, "rightMargin" ) );
  }

  @Test
  public void testRenderRightMarginUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setRightMargin( 5 );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "rightMargin" ) );
  }

  @Test
  public void testRenderInitialBottomMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "bottomMargin" ) == -1 );
  }

  @Test
  public void testRenderBottomMargin() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    clabel.setBottomMargin( 5 );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertEquals( new Integer( 5 ), message.findSetProperty( clabel, "bottomMargin" ) );
  }

  @Test
  public void testRenderBottomMarginUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    clabel.setBottomMargin( 5 );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "bottomMargin" ) );
  }

  @Test
  public void testRenderInitialBackgroundGradient() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    lca.render( clabel );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( clabel );
    assertTrue( operation.getPropertyNames().indexOf( "backgroundGradient" ) == -1 );
  }

  @Test
  public void testRenderBackgroundGradient() throws IOException, JSONException {
    CLabel clabel = new CLabel( shell, SWT.NONE );

    Color[] gradientColors = new Color[] {
      display.getSystemColor( SWT.COLOR_RED ),
      display.getSystemColor( SWT.COLOR_GREEN )
    };
    int[] percents = new int[] { 50 };
    clabel.setBackground( gradientColors , percents );
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    JSONArray gradient = ( JSONArray )message.findSetProperty( clabel, "backgroundGradient" );
    JSONArray colors = ( JSONArray )gradient.get( 0 );
    JSONArray stops = ( JSONArray )gradient.get( 1 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[255,0,0,255]", colors.getJSONArray( 0 ) ) );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,255,0,255]", colors.getJSONArray( 1 ) ) );
    assertEquals( new Integer( 0 ), stops.get( 0 ) );
    assertEquals( new Integer( 50 ), stops.get( 1 ) );
    assertEquals( Boolean.FALSE, gradient.get( 2 ) );
  }

  @Test
  public void testRenderBackgroundGradientUnchanged() throws IOException {
    CLabel clabel = new CLabel( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( clabel );

    Color[] colors = new Color[] {
      display.getSystemColor( SWT.COLOR_RED ),
      display.getSystemColor( SWT.COLOR_GREEN )
    };
    int[] percents = new int[] { 50 };
    clabel.setBackground( colors , percents );
    Fixture.preserveWidgets();
    lca.renderChanges( clabel );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( clabel, "backgroundGradient" ) );
  }
}
