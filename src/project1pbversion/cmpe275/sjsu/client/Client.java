/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package project1pbversion.cmpe275.sjsu.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.example.worldclock.WorldClockServer;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoPayload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

/**
 * Sends a list of continent/city pairs to a {@link WorldClockServer} to
 * get the local times of the specified cities.
 */
/**
 * @author lingzhang
 *
 */
public final class Client {

    static final boolean SSL = System.getProperty("ssl") != null;
    private static final boolean isTest =Configure.isTest;
    static final String BASE_URL = System.getProperty("baseUrl", Configure.BASE_URL);

    public static void main(String[] args) throws Exception {
    	
    	System.out.println("Start Client***************\n");
    	
    	//TODO for get test, need to be removed later
        if(isTest){
        	testGet();
        	testPost();
        }
        //end of test code
        
        
        //TODO create a user interface to input the request and picture information
        
    }
    
    public static void testGet() throws Exception{
    	System.out.println("\ntest with get (read) request-----");	
        Image image = new Image();
        image.setUri(new URI(BASE_URL+"formget"));
        image.setUuid("testuuidforget");
        System.out.println("creat a test image with uuid: " +image.getUuid());
        
        //use a parameter to decide get or post directly
        createChannelAndSendRequest(RequestType.read, image);
    
    	
    }
    
    public static void testPost() throws Exception{
		// TODO Auto-generated method stub
    	System.out.println("\ntest with post (write) request------");	
        Image image = new Image();
        image.setUri(new URI(BASE_URL+"formpost"));
        image.setUuid("testuuidforpost");
        image.setImageName("testNewPicNameforpost");
        System.out.println("creat a test image with uuid: " +image.getUuid());
        
        //use a parameter to decide get or post directly
        createChannelAndSendRequest(RequestType.write, image);
	}
    

    
	/**
	 * @param reqtype
	 * @param img   must contain a valid uri, need to get host and port from uri
	 * @throws Exception
	 */
	public static void createChannelAndSendRequest(RequestType reqtype,Image img) throws Exception{
	
	    EventLoopGroup group = new NioEventLoopGroup();
	    try {
	        Bootstrap b = new Bootstrap();
	        b.group(group)
	         .channel(NioSocketChannel.class)
	         .handler(new ClientInitializer());
	
	        // Make a new connection.
	       String host=img.getUri().getHost();
	       int port=img.getUri().getPort();         
	        Channel ch = b.connect(host, port).sync().channel();
	        
	        
	        //convert img to a Request
	        Request req= createRequest(reqtype, img);
	        
	        System.out.println("image uuid in the request: " +req.getBody().getPhotoPayload().getUuid());
	        System.out.println("image name in the request: " +req.getBody().getPhotoPayload().getName());
	        
	        ch.writeAndFlush(req);
	
	        
	     // Wait for the server to close the connection.
	        ch.closeFuture().sync();
	        
	        ch.close();
	
	
	    } finally {
	        group.shutdownGracefully();
	    }
		
		
	}

	public static Request createRequest(RequestType reqtype, Image img)throws Exception{
	    	
		PhotoPayload.Builder ppb = PhotoPayload.newBuilder()
		 			.setUuid(img.getUuid());
		if(reqtype.equals(RequestType.write)){
			ppb.setName(img.getImageName());
			//TODO add image file data to photoPayload
			//ppb.setData();
		}
		PhotoPayload pp=ppb.build();
		Payload p=Payload.newBuilder().setPhotoPayload(pp).build();
		
		
		PhotoHeader ph= PhotoHeader.newBuilder()
				 		.setRequestType(reqtype)
				 		.build();	         	      	       	    	 
		Header h=Header.newBuilder().setPhotoHeader(ph).build();
		
		
		Request request=Request.newBuilder()
				 				.setHeader(h)
				 				.setBody(p)
				 				.build();
	    
		return request;
	    	
	    }


    	
 
    
    
   
    
    
}
