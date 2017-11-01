package org.packtpub;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserHandler {

	private final UserRepository userRepository;
	
	public UserHandler(UserRepository userRepository){
		this.userRepository = userRepository;
	}
	
	public Mono<ServerResponse> getAllUsers(ServerRequest request){
		Flux<User> users = this.userRepository.getAllUsers();
		return ServerResponse.ok().contentType(APPLICATION_JSON).body(users, User.class);		
	}
	
	public Mono<ServerResponse> getUser(ServerRequest request){
		
		int userId = Integer.valueOf(request.pathVariable("id"));
		
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		Mono<User> userMono = this.userRepository.getUser(userId);
		
		return userMono
				.flatMap(user -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(user)))
				.switchIfEmpty(notFound);		
	}
	
	public Mono<ServerResponse> createUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class);
		return ServerResponse.ok().build(this.userRepository.saveUser(user));
	}
	
	public Mono<ServerResponse> updateUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class);
		return ServerResponse.ok().build(this.userRepository.saveUser(user));
	}
	
	public Mono<ServerResponse> upload(ServerRequest request) {
		
		Mono<Part> part = request.body(BodyExtractors.toMono(Part.class));
		
		//Mono<FilePart> part = request.bodyToMono(FilePart.class);
		
		//part.log().block();
		System.out.println("trap : "+part);//fileName);
		
		//FilePart filePart = part.log().block();
		
		//String contentType = filePart.headers().getContentType().toString();        
        //String fileName = filePart.filename();
        
        System.out.println("inside upload : ");//fileName);        
        //filePart.transferTo(new File("c:\\test\\"+fileName));
		
		return ServerResponse.ok().build();
	}
	
	public Mono<ServerResponse> deleteUser(ServerRequest request) {		
		int userId = Integer.valueOf(request.pathVariable("id"));
		return ServerResponse.ok().build(this.userRepository.deleteUser(userId));
	}
}
