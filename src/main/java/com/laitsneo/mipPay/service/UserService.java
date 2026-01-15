package com.laitsneo.mipPay.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.laitsneo.mipPay.entity.Admin.User;
import com.laitsneo.mipPay.entity.Client.Client;
import com.laitsneo.mipPay.repository.Admin.UserRepository;
import com.laitsneo.mipPay.repository.Client.ClientRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repo;
    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Client> client = this.clientRepository.findByEmail(username);
        Optional<User> admin = this.repo.findByEmail(username);
        if(admin.isPresent()){
            User user = this.repo.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid user name or password !!"));
            System.out.println("admin user Values and creds: "+user);
            return user;
        }else{
            Client client1 = this.clientRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid user name or password !!"));
            System.out.println("Client user Values and creds: "+client1);
            return client1;
        }
    }
    
    
    
}