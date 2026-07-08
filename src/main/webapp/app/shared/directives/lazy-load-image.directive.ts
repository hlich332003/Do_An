import { Directive, ElementRef, Input, OnInit, OnDestroy, Renderer2, inject, OnChanges } from '@angular/core';

@Directive({
  selector: '[jhiLazyLoad]',
  standalone: true,
})
export class LazyLoadImageDirective implements OnInit, OnDestroy, OnChanges {
  @Input('jhiLazyLoad') imageSrc = '';
  @Input() placeholder = 'content/images/default-poster.svg'; // A lightweight placeholder

  private el = inject(ElementRef);
  private renderer = inject(Renderer2);
  private intersectionObserver?: IntersectionObserver;
  private _imageSrc = '';

  ngOnInit(): void {
    this.setPlaceholder();

    if (!this.canObserve()) {
      this.loadImage();
      return;
    }

    this.intersectionObserver = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadImage();
            this.intersectionObserver?.unobserve(this.el.nativeElement);
          }
        });
      },
      {
        rootMargin: '50px',
        threshold: 0.01,
      },
    );

    this.intersectionObserver.observe(this.el.nativeElement);

    if (this.isInViewport()) {
      this.loadImage();
    }
  }

  ngOnChanges(): void {
    if (this.imageSrc && this.imageSrc !== this._imageSrc) {
      this._imageSrc = this.imageSrc;
      this.setPlaceholder();
      if (!this.canObserve() || this.isInViewport()) {
        this.loadImage();
      }
    }
  }

  private canObserve(): boolean {
    return typeof IntersectionObserver !== 'undefined';
  }

  private isInViewport(): boolean {
    const element = this.el.nativeElement as HTMLElement;
    const rect = element.getBoundingClientRect();
    return rect.bottom > -50 && rect.top < (window.innerHeight || document.documentElement.clientHeight) + 50;
  }

  private setPlaceholder(): void {
    if (this.imageSrc && this.el.nativeElement.src !== this.imageSrc) {
      this.renderer.setAttribute(this.el.nativeElement, 'src', this.placeholder);
      this.renderer.addClass(this.el.nativeElement, 'lazy-loading');
    }
  }

  private loadImage(): void {
    if (!this.imageSrc) {
      return;
    }

    const img = new Image();

    img.onload = () => {
      this.renderer.setAttribute(this.el.nativeElement, 'src', this.imageSrc);
      this.renderer.removeClass(this.el.nativeElement, 'lazy-loading');
      this.renderer.addClass(this.el.nativeElement, 'lazy-loaded');
    };

    img.onerror = () => {
      // In case of an error, fallback to a default poster image
      this.renderer.setAttribute(this.el.nativeElement, 'src', 'content/images/default-poster.png');
      this.renderer.removeClass(this.el.nativeElement, 'lazy-loading');
      this.renderer.addClass(this.el.nativeElement, 'lazy-error');
    };

    img.src = this.imageSrc;
  }

  ngOnDestroy(): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
    }
  }
}
