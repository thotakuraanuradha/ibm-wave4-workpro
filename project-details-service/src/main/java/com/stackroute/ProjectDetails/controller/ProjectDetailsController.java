package com.stackroute.ProjectDetails.controller;


import com.stackroute.ProjectDetails.domain.*;
import com.stackroute.ProjectDetails.exceptions.UnauthorizedException;
import com.stackroute.ProjectDetails.listener.Producer;
import com.stackroute.ProjectDetails.exceptions.ProjectAlreadyExistsException;
import com.stackroute.ProjectDetails.exceptions.ProjectBidAlreadyAwardedException;
import com.stackroute.ProjectDetails.exceptions.ProjectDoesNotExistException;
import com.stackroute.ProjectDetails.service.BidService;
import com.stackroute.ProjectDetails.service.ProjectOwnerProjectsServiceImpl;
import com.stackroute.ProjectDetails.service.SkillsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;



@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class ProjectDetailsController {
    private ProjectOwnerProjectsServiceImpl projectOwnerProjectsService;
    private SkillsServiceImpl skillsService;
    private  Producer producer;
    private BidService bidService;


    @Autowired
    public ProjectDetailsController(ProjectOwnerProjectsServiceImpl projectOwnerProjectsService,SkillsServiceImpl skillsService,Producer producer,BidService bidService) {
        this.projectOwnerProjectsService = projectOwnerProjectsService;
        this.skillsService=skillsService;
        this.producer=producer;
        this.bidService=bidService;
    }


    /**
     * Project Owner searches his projects by id
     * @param projectId will be the project id which is automatic assigned to project while adding Projects
     * @param projectOwnerId will be Project Owner email id
     * @return ProjectDetails if it is there otherWise it will return not found
     */
    @GetMapping("/projectOwner/{projectOwnerId}/projects/{projectId}")
    public ResponseEntity<?> projectDetailsForFreelancer(@PathVariable("projectId") String projectId, @PathVariable("projectOwnerId") String projectOwnerId) {

        ProjectsOfProjectOwner allProjectsOfProjectOwner = projectOwnerProjectsService.getProjectsByEmailId(projectOwnerId);
        for (ProjectDetails details : allProjectsOfProjectOwner.getProjectDetailsList()) {
            //List<ProjectDetails> projectDetailsList = allProjectsOfProjectOwner.getProjectDetailsList();
                if (details.getProjectId().equals(projectId)){
                return new ResponseEntity<ProjectDetails>(details, HttpStatus.OK);
            }
        }
        return new ResponseEntity<String>("Project Not Found", HttpStatus.OK);
    }

    /**
     * When Freelancer searches the Projects by SkillName
     * @param skillName skills
     * @return List of Projects
     */
    @GetMapping("/skill/{skillName}/projects")
    public ResponseEntity<?> freelancerProjectSearch(@PathVariable String skillName) {

        return new ResponseEntity< List<ProjectDetails>>(skillsService.searchBySkill(skillName), HttpStatus.OK);
    }
// freelancer can bid on a project but if it is null then problem
    /**
     *Freelancers bid on a project
     * @param header used for validation
     * @param request used for validation
     * @param bidOfFreelancer freelancer details
     * @param projectId freelancer bid on a project
     * @param projectOwnerId project owner email id
     * @return  Freelancer  bid on a project
     * @throws ProjectDoesNotExistException
     * @throws UnauthorizedException
     */
    @PostMapping("/projectOwner/{projectOwnerId}/projects/{projectId}/bid")
    public ResponseEntity<?> freelancerBidOnAProject (@RequestHeader HttpHeaders header, HttpServletRequest request, @RequestBody BidOfFreelancer bidOfFreelancer, @PathVariable("projectId") String projectId, @PathVariable("projectOwnerId") String projectOwnerId) throws ProjectDoesNotExistException, UnauthorizedException {

        //long contentLength = header.getContentLength();
        //String token=request.getHeader("token");
        //if(token!=null){
            ProjectsOfProjectOwner allProjectsOfProjectOwner = projectOwnerProjectsService.getProjectsByEmailId(projectOwnerId);
            for (ProjectDetails details : allProjectsOfProjectOwner.getProjectDetailsList()) {
                if (details.getProjectId().equals(projectId) ){
                  //  System.out.println(details.getAllBidsOfFreelancers());

                    if(details.getAllBidsOfFreelancers().isEmpty())
                    {
                        List<BidOfFreelancer> bidOfFreelancers = details.getAllBidsOfFreelancers();
                        bidOfFreelancers.add(bidOfFreelancer);
                        details.setAllBidsOfFreelancers(bidOfFreelancers);
                    }
                    else {
                        if(details.getProjectStatus().equals("closed"))
                        {
                            return new ResponseEntity<String>(" You(Freelancer)  can't bid. Project has been closed", HttpStatus.OK);
                        }
                        else if(details.getAllBidsOfFreelancers().get(0).getFreelancerEmailId().equals(bidOfFreelancer.getFreelancerEmailId()))
                        {
                            return new ResponseEntity<String>(" You(Freelancer)  can't bid more than once", HttpStatus.OK);
                        }
                        else {
                            List<BidOfFreelancer> bidOfFreelancers = details.getAllBidsOfFreelancers();
                            bidOfFreelancers.add(bidOfFreelancer);
                            details.setAllBidsOfFreelancers(bidOfFreelancers);
                        }
                    }
                    projectOwnerProjectsService.addProjects(allProjectsOfProjectOwner);
                    break;
                }
            }
            return new ResponseEntity<String>(" You(Freelancer)  bid on a project", HttpStatus.OK);
       // }
        //else
          //  throw new UnauthorizedException("Login Please");

    }

    /**
     * Freelancer has won the project bid
     * @return Freelancer has won the project bid
     */
    @GetMapping("projects/projectId/bid/won")
    public ResponseEntity<?> freelancerBidsWon() {
        return new ResponseEntity<String>("Freelancer has won the project bid", HttpStatus.OK);
    }


    /**
     * Project Owner add his projects
     * Method also add projects with respect to skills in skills collections
     * @param projectsOfProjectOwner all the details provided by project owner
     * @return Project owner adds a project
     * @throws ProjectAlreadyExistsException
     */
    @PostMapping("/projects/project")
    public ResponseEntity<?> addProject(@RequestBody ProjectsOfProjectOwner projectsOfProjectOwner ) throws ProjectAlreadyExistsException  {

        ProjectsOfProjectOwner allProjectsOfProjectOwner = projectOwnerProjectsService.getProjectsByEmailId(projectsOfProjectOwner.getProjectOwnerEmailId());
        if(allProjectsOfProjectOwner!=null) {
            for (ProjectDetails projectDetails : allProjectsOfProjectOwner.getProjectDetailsList()) {
                if (projectDetails.getProjectId().equals(projectsOfProjectOwner.getProjectDetailsList().get(0).getProjectId())) {
                    throw new ProjectAlreadyExistsException();
                }
            }
        }

        if (allProjectsOfProjectOwner == null) {
            projectOwnerProjectsService.addProjects(projectsOfProjectOwner);

        } else {
            allProjectsOfProjectOwner.getProjectDetailsList().add(projectsOfProjectOwner.getProjectDetailsList().get(0));
            projectOwnerProjectsService.addProjects(allProjectsOfProjectOwner);
        }

        for(ProjectDetails details: projectsOfProjectOwner.getProjectDetailsList())
        {
            for(String skillsFromProjectOwner:details.getSkillsSetList())
            {
                Skills oldSkills= skillsService.findBySkillName(skillsFromProjectOwner);
                if(oldSkills==null)
                {
                    Skills newSkills = new Skills();
                    newSkills.setSkillName(skillsFromProjectOwner);
                    List<ProjectDetails> projectDetailsList = new ArrayList<>();
                    projectDetailsList.add(details);
                    newSkills.setProjectDetailsList(projectDetailsList);
                    this.skillsService.addProjectWrtSkills(newSkills);
                }
                else
                {
                    oldSkills.getProjectDetailsList().add(projectsOfProjectOwner.getProjectDetailsList().get(0));
                    this.skillsService.addProjectWrtSkills(oldSkills);
                }
            }
        }
        return new ResponseEntity<String>("Project owner adds a project", HttpStatus.OK);
    }


    /**
     * Project Owner gets all his projects
     * @param projectOwnerId project owner email id
     * @return all his projects
     */
    @GetMapping("/projectOwner/{projectOwnerId}/project")
    public ResponseEntity<ProjectsOfProjectOwner> projectOwnerViewsAllProjects(@PathVariable String projectOwnerId) {
        ProjectsOfProjectOwner allProjectsOfProjectOwner = projectOwnerProjectsService.getProjectsByEmailId(projectOwnerId);
        return new ResponseEntity<ProjectsOfProjectOwner>(allProjectsOfProjectOwner, HttpStatus.OK);
    }


    /**
     * When Project Owner accepts the bid from the freelancer
     * project status will be changed to closed
     * and project awarded will be true
     * @param email freelancer email id
     * @param projectId
     * @param projectOwnerId
     * @return Project owner accepts a bid
     * @throws ProjectBidAlreadyAwardedException
     */
    @PutMapping("/projectOwner/{projectOwnerId}/projects/{projectId}/bid/accept/{freelancerEmail}")
    public ResponseEntity<?> ownerAcceptsBid(@PathVariable("freelancerEmail") String email,@PathVariable("projectId") String projectId, @PathVariable("projectOwnerId") String projectOwnerId) throws ProjectBidAlreadyAwardedException {
        ProjectsOfProjectOwner allProjectsOfProjectOwner = projectOwnerProjectsService.getProjectsByEmailId(projectOwnerId);
        List<BidKafka> listBid=bidService.getAll();
        for (ProjectDetails details : allProjectsOfProjectOwner.getProjectDetailsList()) {
            if (details.getProjectId().equals(projectId)) {

                details.setProjectStatus("closed");

                for (BidOfFreelancer freelancer : details.getAllBidsOfFreelancers()) {
                    if (freelancer.getFreelancerEmailId().equals(email)) {
                        freelancer.setProjectAwarded(true);
                        System.out.println(freelancer.isProjectAwarded());
                        this.projectOwnerProjectsService.addProjects(allProjectsOfProjectOwner);
                        break;
                    }
                }
                break;
            }
        }

        for(BidKafka bid:listBid) {
            if (bid.getFreelancerEmail().equals(email)){
                bid.setAwarded(true);
                producer.send(bid);
                System.out.println(bid+"produced..........................................");
            }
        }
        return new ResponseEntity<String>("Project owner accepts a bid", HttpStatus.OK);
    }


    /**
     * Project owner sees all the bids by freelancer on a particular projects
     * @param projectId id of projects
     * @param projectOwnerId
     * @return if the project is available it will return List of bids of freelancer  otherwise projects not found
     */
    @GetMapping("/projectOwner/{projectOwnerId}/projects/{projectId}/bids")
    public ResponseEntity<?> projectOwnerViewsAllBids(@PathVariable("projectId") String projectId, @PathVariable("projectOwnerId") String projectOwnerId) {
        ProjectsOfProjectOwner list = projectOwnerProjectsService.getProjectsByEmailId(projectOwnerId);
        for (ProjectDetails details : list.getProjectDetailsList()) {
            if (details.getProjectId().equals(projectId)){
                List<BidOfFreelancer> bidOfFreelancers = details.getAllBidsOfFreelancers();
                return new ResponseEntity<List<BidOfFreelancer>>(bidOfFreelancers, HttpStatus.OK);
            }
        }
        return new ResponseEntity<String>("Project Not Found", HttpStatus.OK);
    }
}
