package com.example.demo.src.user.model.Res;

import com.example.demo.src.user.model.OtherAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetUserAddressRes {
    private String homeAddress;
    private String homeDetail;
    private String homeGuide;
    private String companyAddress;
    private String companyDetail;
    private String companyGuide;
    private List<OtherAddress> otherAddress;
}
