package com.example.demo.src.user.model.Res;

import com.example.demo.src.user.model.AddressInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetUserAddressRes {
    private AddressInfo nowAddress;
    private AddressInfo homeAddress;
    private AddressInfo companyAddress;
    private List<AddressInfo> otherAddress;
}
