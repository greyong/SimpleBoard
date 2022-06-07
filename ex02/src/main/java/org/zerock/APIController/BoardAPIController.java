package org.zerock.APIController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;
import org.zerock.domain.PageDTO;
import org.zerock.service.BoardService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@Log4j
@RequestMapping("/api/board/*")
@AllArgsConstructor
public class BoardAPIController {
	
	private BoardService service;
	
	@RequestMapping("/list")
	public ResponseEntity<Object> list(Criteria cri) {
		log.info("list" + cri);

		int total = service.getTotal(cri);
		
		log.info("total: " + total);
			
		return new ResponseEntity<>(service.getList(cri), HttpStatus.OK);
	}
	
	@RequestMapping("/register")
	public ResponseEntity<String> register(BoardVO board, RedirectAttributes rttr) {
		log.info("register" + board);
		
		if(board.getAttachList() != null) {
			board.getAttachList().forEach(attach -> log.info(attach));
		}
		
		service.register(board);
		
		rttr.addFlashAttribute("result", board.getBno());
		
		return new ResponseEntity<>("success", HttpStatus.OK);
		
		
	}
	
	@GetMapping("/register")
	public void register() {
				
	}
	
	@PostMapping("/modify")
	public ResponseEntity<String> modify(BoardVO board) {
		log.info("modify" + board);
		
		if(service.modify(board)) {
			return new ResponseEntity<>("success", HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping({"/get","/modify"})
	public ResponseEntity<BoardVO> get(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri) {
		log.info("/get or /modify");
				
		return new ResponseEntity<>(service.get(bno), HttpStatus.OK);
	}
	
	@PostMapping("/remove")
	public ResponseEntity<String> remove(@RequestParam("bno") Long bno) {
		log.info("/remove" + bno);
		
		List<BoardAttachVO> attachList = service.getAttachList(bno);
		
		if(service.remove(bno)) {
			
			deleteFiles(attachList);
			return new ResponseEntity<>("success", HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		rttr.addAttribute("pageNum", cri.getPageNum());
//		rttr.addAttribute("amount", cri.getAmount());
//		rttr.addAttribute("type", cri.getType());
//		rttr.addAttribute("keyword", cri.getKeyword());
		
		//return "redirect:/board/list" + cri.getListLink();
		
	}
	
	@GetMapping(value = "/getAttachList", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	@ResponseBody
	public ResponseEntity<List<BoardAttachVO>> getAttachList(Long bno){
		log.info("getAttachList:"+bno);
		
		return new ResponseEntity<>(service.getAttachList(bno), HttpStatus.OK);
		
	}
	
	private void deleteFiles(List<BoardAttachVO> attachList) {
		if(attachList == null || attachList.size() == 0) {
			return;
		}
		
		log.info("delete attach files....................................");
		log.info(attachList);
		
		attachList.forEach(attach -> {
			try {
				Path file = Paths.get("c:\\upload\\"+attach.getUploadPath()+"\\"+attach.getUuid()+"_"+attach.getFileName());
				
				Files.deleteIfExists(file);
				
				// TODO docx파일의 경우 null에러 발생
				if(Files.probeContentType(file).startsWith("image")) {
					Path thumbNail = Paths.get("c:\\upload\\"+attach.getUploadPath()+"\\s_"+attach.getUuid()+"_"+attach.getFileName());
					
					Files.deleteIfExists(thumbNail);
				}
				
				
			} catch (Exception e) {
				log.error("delete file error" + e.getMessage());
			}
		});
	}


}
