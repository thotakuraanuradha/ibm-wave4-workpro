import { Component, OnInit } from '@angular/core';
import { FreelancerDetailsService} from '../freelancer-details.service'
import { from } from 'rxjs';
@Component({
  selector: 'app-freelancerdetails',
  templateUrl: './freelancerdetails.component.html',
  styleUrls: ['./freelancerdetails.component.scss']
})
export class FreelancerdetailsComponent implements OnInit {

  constructor(private fservice: FreelancerDetailsService) { }
  skills = [];
  domains =[];
  samplelinks =[];
  // domainName:any;
  ngOnInit() {


  }
  save(data) {
    data.skills = this.skills
    data.domain = this.domains
    data.link =this.samplelinks
    this.fservice.setDetailsofFreelancers(data).subscribe(console.log)
  }

  // save1(data1) {
  //   data1.domains = this.domains
  //   this.fservice.setDetailsofFreelancers(data1).subscribe(console.log)
  // }
  
  addSkill(skill) {
    let skillDetails = {
      yearsOfExp: skill.SkillExp,
      level: skill.level,
      skillName: skill.SkillName
    }
    this.skills.push(skillDetails)
  }

  addDomain(domain){
    let domainDetails = {
      domainName:domain.DomainName,
      yearsOfExp:domain.DomainExperience
    }
     this.domains.push(domainDetails)
  }

  addLink(samplelink){
    let linkDetails = {
      link:samplelink.Link
    }
    this.samplelinks.push(linkDetails)
  }
  deleteSkill(skill){
    this.skills = this.skills.filter(e => e.SkillName !== skill.SkillName)
  }
}