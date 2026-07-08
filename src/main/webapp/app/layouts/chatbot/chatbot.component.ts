import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { faTimes, faComments, faPaperPlane, faTrashAlt } from '@fortawesome/free-solid-svg-icons';

interface TinNhan {
  text: string;
  sender: 'user' | 'bot';
}

@Component({
  selector: 'jhi-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, FaIconComponent, HttpClientModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss'],
})
export class ChatbotComponent implements OnInit, AfterViewChecked {
  faTimes = faTimes;
  faComments = faComments;
  faPaperPlane = faPaperPlane;
  faTrashAlt = faTrashAlt;

  isOpen = false;
  dangTai = false;
  cauHoi = '';
  tinNhanList: TinNhan[] = [];

  @ViewChild('chatBody') private chatBodyRef?: ElementRef;

  private http = inject(HttpClient);

  ngOnInit(): void {
    const saved = localStorage.getItem('chatbot_history');
    if (saved) {
      try {
        this.tinNhanList = JSON.parse(saved);
      } catch (e) {
        this.loadDefaultWelcome();
      }
    } else {
      this.loadDefaultWelcome();
    }
  }

  private loadDefaultWelcome(): void {
    this.tinNhanList = [{ text: 'Chào bạn, tôi là trợ lý AI của CinemaTick. Tôi có thể giúp gì cho bạn?', sender: 'bot' }];
    this.saveToStorage();
  }

  private saveToStorage(): void {
    try {
      localStorage.setItem('chatbot_history', JSON.stringify(this.tinNhanList));
    } catch (e) {
      // Ignored
    }
  }

  clearHistory(): void {
    if (confirm('Bạn có muốn xóa lịch sử chat này không?')) {
      this.loadDefaultWelcome();
    }
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    if (this.chatBodyRef) {
      try {
        this.chatBodyRef.nativeElement.scrollTop = this.chatBodyRef.nativeElement.scrollHeight;
      } catch (err) {
        // Ignored
      }
    }
  }

  toggleChat(state?: boolean): void {
    this.isOpen = state !== undefined ? state : !this.isOpen;
  }

  guiTinNhan(): void {
    if (!this.cauHoi.trim()) {
      return;
    }

    // Add user message to chat
    this.tinNhanList.push({ text: this.cauHoi, sender: 'user' });
    this.saveToStorage();
    const userQuestion = this.cauHoi;
    this.cauHoi = '';
    this.dangTai = true;

    // Call backend API
    this.http
      .post<{ reply: string }>('/api/chatbot/ask', { message: userQuestion })
      .pipe(finalize(() => (this.dangTai = false)))
      .subscribe({
        next: data => {
          this.tinNhanList.push({ text: data.reply, sender: 'bot' });
          this.saveToStorage();
        },
        error: () => {
          this.tinNhanList.push({ text: 'Dạ, kết nối tới tổng đài AI bị gián đoạn.', sender: 'bot' });
          this.saveToStorage();
        },
      });
  }
}
